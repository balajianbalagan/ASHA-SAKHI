package com.littleb01s.ashasakhichat.data.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer

// Data class to hold risk assessment results
data class RiskAssessmentResult(
    val riskLevel: String,
    val observations: Map<String, String>
)

class RiskPredictor(private val context: Context) {
    private val featureMapping = mutableMapOf<String, String>()
    private val labelMapping = mutableMapOf<String, String>()
    private val reverseLabelMapping = mutableMapOf<Int, String>()

    init {
        loadMappings()
    }

    private fun loadMappings() {
        // Load feature mapping
        context.assets.open("feature_mapping.json").use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonObject = JSONObject(reader.readText())
            jsonObject.keys().forEach { key ->
                featureMapping[key] = jsonObject.getString(key)
            }
        }

        // Load label mapping
        context.assets.open("label_mapping.json").use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonObject = JSONObject(reader.readText())
            jsonObject.keys().forEach { key ->
                val value = jsonObject.getInt(key)
                labelMapping[key] = value.toString()
                reverseLabelMapping[value] = key
            }
        }
    }

    fun assessRisk(
        age: Float,
        systolicBP: Float,
        diastolicBP: Float,
        bloodSugar: Float,
        bodyTemp: Float,
        heartRate: Float
    ): RiskAssessmentResult {
        // Collect observations for each parameter
        val observations = mutableMapOf<String, String>()
        
        // Age assessment
        observations["Age (${age.toInt()} years)"] = when {
            age < 18 -> "High risk due to incomplete physical development. Recommended age for pregnancy is 18-35 years."
            age in 18.0..35.0 -> "Optimal age range with lowest typical risks."
            age > 35 -> "Higher risk for complications like gestational diabetes, hypertension, and chromosomal abnormalities. Your age (${age.toInt()}) is ${(age - 35).toInt()} years above the optimal range."
            else -> "Invalid age value"
        }
        
        // Systolic BP assessment
        observations["Systolic BP (${systolicBP.toInt()} mmHg)"] = when {
            systolicBP < 90 -> "Low blood pressure; may lead to dizziness and poor fetal circulation. Your reading is ${(90 - systolicBP).toInt()} mmHg below the normal range (90-120 mmHg)."
            systolicBP in 90.0..120.0 -> "Ideal systolic range."
            systolicBP in 121.0..139.0 -> "Pre-hypertensive stage. Your reading is ${(systolicBP - 120).toInt()} mmHg above the ideal range (90-120 mmHg)."
            systolicBP >= 140 -> "Risk of pre-eclampsia and organ damage. Your reading is ${(systolicBP - 120).toInt()} mmHg above the ideal range (90-120 mmHg)."
            else -> "Invalid systolic BP value"
        }
        
        // Diastolic BP assessment
        observations["Diastolic BP (${diastolicBP.toInt()} mmHg)"] = when {
            diastolicBP < 60 -> "Can indicate hypotension, leading to fainting or fatigue. Your reading is ${(60 - diastolicBP).toInt()} mmHg below the normal range (60-80 mmHg)."
            diastolicBP in 60.0..80.0 -> "Normal range."
            diastolicBP in 81.0..89.0 -> "Pre-hypertension. Your reading is ${(diastolicBP - 80).toInt()} mmHg above the normal range (60-80 mmHg)."
            diastolicBP >= 90 -> "High diastolic pressure increases risk of pre-eclampsia and placental issues. Your reading is ${(diastolicBP - 80).toInt()} mmHg above the normal range (60-80 mmHg)."
            else -> "Invalid diastolic BP value"
        }
        
        // Blood Sugar assessment
        observations["Blood Sugar (${bloodSugar} mmol/L)"] = when {
            bloodSugar < 3 -> "Hypoglycemia; can cause fainting or seizures. Your reading is ${String.format("%.1f", 3 - bloodSugar)} mmol/L below the normal range (4-7 mmol/L)."
            bloodSugar in 3.0..3.9 -> "Below normal range; monitor for symptoms of hypoglycemia. Your reading is ${String.format("%.1f", 4 - bloodSugar)} mmol/L below the normal range (4-7 mmol/L)."
            bloodSugar in 4.0..7.0 -> "Normal fasting blood sugar."
            bloodSugar in 7.1..11.0 -> "Impaired glucose tolerance. Your reading is ${String.format("%.1f", bloodSugar - 7)} mmol/L above the normal range (4-7 mmol/L)."
            bloodSugar > 11 -> "Gestational diabetes likely; requires dietary and possibly insulin management. Your reading is ${String.format("%.1f", bloodSugar - 7)} mmol/L above the normal range (4-7 mmol/L)."
            else -> "Invalid blood sugar value"
        }
        
        // Body Temperature assessment
        observations["Body Temperature (${bodyTemp}°F)"] = when {
            bodyTemp < 95 -> "Hypothermia risk; impacts fetal development. Your temperature is ${String.format("%.1f", 97 - bodyTemp)}°F below the normal range (97-99.5°F)."
            bodyTemp in 95.0..96.9 -> "Below normal range; monitor for hypothermia. Your temperature is ${String.format("%.1f", 97 - bodyTemp)}°F below the normal range (97-99.5°F)."
            bodyTemp in 97.0..99.5 -> "Normal range."
            bodyTemp in 99.6..100.3 -> "Slight elevation, monitor for infection. Your temperature is ${String.format("%.1f", bodyTemp - 99.5)}°F above the normal range (97-99.5°F)."
            bodyTemp > 100.4 -> "Possible fever due to infection; medical attention needed. Your temperature is ${String.format("%.1f", bodyTemp - 99.5)}°F above the normal range (97-99.5°F)."
            else -> "Invalid body temperature value"
        }
        
        // Heart Rate assessment
        observations["Heart Rate (${heartRate.toInt()} bpm)"] = when {
            heartRate < 60 -> "Bradycardia; may indicate heart conduction issues. Your heart rate is ${(60 - heartRate).toInt()} bpm below the normal range (60-100 bpm)."
            heartRate in 60.0..100.0 -> "Normal resting heart rate."
            heartRate in 101.0..120.0 -> "Mild tachycardia (can be normal during pregnancy). Your heart rate is ${(heartRate - 100).toInt()} bpm above the normal range (60-100 bpm)."
            heartRate > 120 -> "May signal infection, dehydration, or anemia. Your heart rate is ${(heartRate - 100).toInt()} bpm above the normal range (60-100 bpm)."
            else -> "Invalid heart rate value"
        }
        
        // Get the risk prediction from the ONNX model
        val riskLevel = predictRiskLevel(age, systolicBP, diastolicBP, bloodSugar, bodyTemp, heartRate) ?: "Unknown"
        
        return RiskAssessmentResult(riskLevel, observations)
    }

    fun predictRiskLevel(
        age: Float,
        systolicBP: Float,
        diastolicBP: Float,
        bloodSugar: Float,
        bodyTemp: Float,
        heartRate: Float
    ): String? {
        try {
            // Get the environment
            val ortEnvironment = OrtEnvironment.getEnvironment()
            
            // Get the session
            val ortSession = getOrtSession(context, ortEnvironment)
            
            // Create input array in the correct order based on feature mapping
            val orderedFeatures = mutableListOf<Float>()
            featureMapping.entries.sortedBy { it.key }.forEach { (_, feature) ->
                orderedFeatures.add(when (feature) {
                    "Age" -> age
                    "SystolicBP" -> systolicBP
                    "DiastolicBP" -> diastolicBP
                    "BS" -> bloodSugar
                    "BodyTemp" -> bodyTemp
                    "HeartRate" -> heartRate
                    else -> 0f
                })
            }
            
            // Run inference
            val result = run(orderedFeatures.toFloatArray(), ortSession, ortEnvironment)
            
            // Get the risk level label
            return reverseLabelMapping[result.toInt()]
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getOrtSession(
        context: Context, 
        ortEnvironment: OrtEnvironment
    ): OrtSession {
        // Reads the model bytes from the assets
        val modelBytes = context.assets.open("xgboost_risk_predictor.onnx").readBytes()
        // Creates a session using the model bytes
        return ortEnvironment.createSession(modelBytes)
    }

    // Make predictions with given inputs
    private fun run(
        inputs: FloatArray, 
        ortSession: OrtSession, 
        ortEnvironment: OrtEnvironment
    ): Float {
        // Get the name of the input and output nodes
        val inputName = ortSession.inputNames?.iterator()?.next() ?: "input"
        val outputName = HashSet(ortSession.outputNames)
        
        // Make a FloatBuffer of the inputs
        val floatBufferInputs = FloatBuffer.wrap(inputs)
        
        // Create input tensor
        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            floatBufferInputs,
            longArrayOf(1, inputs.size.toLong()) // shape of input tensor
        )
        
        // Run the model
        val results = ortSession.run(mapOf(inputName to inputTensor), outputName)
        
        // Get the output tensor
        val outputTensor = results[0]
        
        // Handle different output types
        return when (val outputValue = outputTensor.value) {
            is Array<*> -> {
                when (val firstElement = outputValue[0]) {
                    is Array<*> -> (firstElement[0] as Number).toFloat()
                    is Number -> firstElement.toFloat()
                    is LongArray -> firstElement[0].toFloat()
                    else -> throw IllegalStateException("Unexpected output type: ${firstElement?.javaClass}")
                }
            }
            is Number -> outputValue.toFloat()
            is LongArray -> outputValue[0].toFloat()
            else -> throw IllegalStateException("Unexpected output type: ${outputValue.javaClass}")
        }
    }
} 