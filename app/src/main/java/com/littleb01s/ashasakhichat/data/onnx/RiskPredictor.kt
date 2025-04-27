package com.littleb01s.ashasakhichat.data.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer

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