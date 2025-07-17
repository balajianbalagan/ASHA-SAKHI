package com.littleb01s.ashasakhichat.utils

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

object ASHAJsonReader {
    private const val TAG = "ASHAJsonReader"

    /**
     * Reads ASHA guidelines from JSON file with PDF fallback
     */
    fun readASHAGuidelines(context: Context, jsonFilePath: String, pdfFallbackPath: String): List<String> {
        return try {
            // Try JSON first
            val jsonChunks = readJsonGuidelines(jsonFilePath)
            if (jsonChunks.isNotEmpty()) {
                Log.d(TAG, "Successfully loaded ${jsonChunks.size} JSON chunks")
                return jsonChunks
            } else {
                Log.w(TAG, "JSON file empty or invalid, falling back to PDF")
                // Fallback to PDF
                PDFReader.readPDFAsStructuredChunks(pdfFallbackPath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading JSON, falling back to PDF: ${e.message}")
            try {
                PDFReader.readPDFAsStructuredChunks(pdfFallbackPath)
            } catch (pdfError: Exception) {
                Log.e(TAG, "Both JSON and PDF failed: ${pdfError.message}")
                emptyList()
            }
        }
    }

    /**
     * Reads and parses JSON guidelines into structured chunks
     */
    private fun readJsonGuidelines(jsonFilePath: String): List<String> {
        val file = File(jsonFilePath)
        if (!file.exists()) {
            Log.e(TAG, "JSON file not found at: $jsonFilePath")
            return emptyList()
        }

        val jsonContent = file.readText()
        val jsonObject = JSONObject(jsonContent)
        
        val chunks = mutableListOf<String>()
        
        // Parse different sections of ASHA guidelines
        parsePregnancyCare(jsonObject, chunks)
        parseNewbornCare(jsonObject, chunks)
        parseImmunization(jsonObject, chunks)
        parseNutrition(jsonObject, chunks)
        parsePregnancySchemes(jsonObject, chunks)
        parseFinancialSupportSchemes(jsonObject, chunks)
        parseClinicalProtocols(jsonObject, chunks)

        Log.d(TAG, "Total chunks created: ${chunks.size}")
        if (chunks.isNotEmpty()) {
            Log.d(TAG, "Sample chunk: ${chunks[0]}")
        } else {
            Log.w(TAG, "No chunks created from JSON!")
        }
        
        return chunks
    }

    private fun parsePregnancyCare(jsonObject: JSONObject, chunks: MutableList<String>) {
        val pregnancySection = jsonObject.optJSONObject("pregnancy_care")
        if (pregnancySection != null) {
            // Parse ANC visits
            val ancVisits = pregnancySection.optJSONObject("anc_visits")
            if (ancVisits != null) {
                ancVisits.keys().forEach { key ->
                    val visit = ancVisits.getJSONObject(key)
                    val chunk = createPregnancyChunk(visit, "anc_visit_$key")
                    addChunkWithSizeLimit(chunk, chunks)
                }
            }

            // Parse complications
            val complications = pregnancySection.optJSONObject("complications")
            if (complications != null) {
                complications.keys().forEach { key ->
                    val complication = complications.getJSONObject(key)
                    val chunk = createComplicationChunk(complication, "complication_$key")
                    addChunkWithSizeLimit(chunk, chunks)
                }
            }

            // Parse general care
            val general = pregnancySection.optJSONObject("general_care")
            if (general != null) {
                val chunk = createGeneralPregnancyChunk(general)
                addChunkWithSizeLimit(chunk, chunks)
            }

            // Parse abortion care
            val abortion = pregnancySection.optJSONObject("abortion_care")
            if (abortion != null) {
                val chunk = createAbortionCareChunk(abortion)
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parseDeliveryCare(jsonObject: JSONObject, chunks: MutableList<String>) {
        val deliverySection = jsonObject.optJSONObject("delivery_care")
        if (deliverySection != null) {
            deliverySection.keys().forEach { key ->
                val procedure = deliverySection.getJSONObject(key)
                val chunk = createDeliveryChunk(procedure, "delivery_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parsePostpartumCare(jsonObject: JSONObject, chunks: MutableList<String>) {
        val postpartumSection = jsonObject.optJSONObject("postpartum_care")
        if (postpartumSection != null) {
            postpartumSection.keys().forEach { key ->
                val care = postpartumSection.getJSONObject(key)
                val chunk = createPostpartumChunk(care, "postpartum_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parseNewbornCare(jsonObject: JSONObject, chunks: MutableList<String>) {
        val newbornSection = jsonObject.optJSONObject("newborn_care")
        if (newbornSection != null) {
            newbornSection.keys().forEach { key ->
                val care = newbornSection.getJSONObject(key)
                val chunk = createNewbornChunk(care, "newborn_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parseImmunization(jsonObject: JSONObject, chunks: MutableList<String>) {
        val immunizationSection = jsonObject.optJSONObject("immunization")
        if (immunizationSection != null) {
            immunizationSection.keys().forEach { key ->
                val item = immunizationSection.getJSONObject(key)
                val chunk = createImmunizationChunk(item, "immunization_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parseNutrition(jsonObject: JSONObject, chunks: MutableList<String>) {
        val nutritionSection = jsonObject.optJSONObject("nutrition")
        if (nutritionSection != null) {
            nutritionSection.keys().forEach { key ->
                val guideline = nutritionSection.getJSONObject(key)
                val chunk = createNutritionChunk(guideline, "nutrition_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parseEmergencyCare(jsonObject: JSONObject, chunks: MutableList<String>) {
        val emergencySection = jsonObject.optJSONObject("emergency_care")
        if (emergencySection != null) {
            val emergencies = emergencySection.optJSONObject("emergencies")
            if (emergencies != null) {
                emergencies.keys().forEach { key ->
                    val emergency = emergencies.getJSONObject(key)
                    val chunk = createEmergencyChunk(emergency, "emergency_$key")
                    addChunkWithSizeLimit(chunk, chunks)
                }
            }
        }
    }

    private fun parseGeneralGuidelines(jsonObject: JSONObject, chunks: MutableList<String>) {
        val generalSection = jsonObject.optJSONObject("general_guidelines")
        if (generalSection != null) {
            generalSection.keys().forEach { key ->
                val guideline = generalSection.getJSONObject(key)
                val chunk = createGeneralGuidelineChunk(guideline, "general_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parsePregnancySchemes(jsonObject: JSONObject, chunks: MutableList<String>) {
        val schemesSection = jsonObject.optJSONObject("pregnancy_schemes")
        if (schemesSection != null) {
            schemesSection.keys().forEach { schemeCategory ->
                val category = schemesSection.getJSONObject(schemeCategory)
                category.keys().forEach { schemeKey ->
                    val scheme = category.getJSONObject(schemeKey)
                    val chunk = createPregnancySchemeChunk(scheme, "scheme_${schemeCategory}_$schemeKey")
                    addChunkWithSizeLimit(chunk, chunks)
                }
            }
        }
    }

    private fun parseFinancialSupportSchemes(jsonObject: JSONObject, chunks: MutableList<String>) {
        val pregnancySchemesSection = jsonObject.optJSONObject("pregnancy_schemes")
        if (pregnancySchemesSection != null) {
            val financialSupportSection = pregnancySchemesSection.optJSONObject("financial_support_schemes")
            if (financialSupportSection != null) {
                financialSupportSection.keys().forEach { schemeKey ->
                    val scheme = financialSupportSection.getJSONObject(schemeKey)
                    val chunk = createFinancialSupportSchemeChunk(scheme, "financial_support_$schemeKey")
                    addChunkWithSizeLimit(chunk, chunks)
                }
            }
        }
    }

    private fun parseClinicalProtocols(jsonObject: JSONObject, chunks: MutableList<String>) {
        val protocolsSection = jsonObject.optJSONObject("clinical_protocols")
        if (protocolsSection != null) {
            protocolsSection.keys().forEach { key ->
                val protocol = protocolsSection.getJSONObject(key)
                val chunk = createClinicalProtocolChunk(protocol, "protocol_$key")
                addChunkWithSizeLimit(chunk, chunks)
            }
        }
    }

    private fun parseHealthcareInfrastructure(jsonObject: JSONObject, chunks: MutableList<String>) {
        val infrastructureSection = jsonObject.optJSONObject("healthcare_infrastructure")
        if (infrastructureSection != null) {
            val chunk = createHealthcareInfrastructureChunk(infrastructureSection)
            addChunkWithSizeLimit(chunk, chunks)
        }
    }

    // Helper methods to create structured chunks
    private fun createPregnancyChunk(visit: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", visit.optString("title", "ANC Visit"))
            put("content", visit.optString("description", ""))
            put("key_points", JSONArray().apply {
                val keyProcedures = visit.optString("key_procedures", "")
                if (keyProcedures.isNotEmpty()) {
                    put(keyProcedures)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "pregnancy_care")
            put("chunk_id", id)
            put("visit_type", "anc_visit")
        }.toString()
    }

    private fun createComplicationChunk(complication: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", complication.optString("title", "Pregnancy Complication"))
            put("content", complication.optString("description", ""))
            put("key_points", JSONArray().apply {
                val symptoms = complication.optString("symptoms", "")
                if (symptoms.isNotEmpty()) {
                    put(symptoms)
                }
            })
            put("guidelines", JSONArray().apply {
                val management = complication.optString("management", "")
                if (management.isNotEmpty()) {
                    put(management)
                }
            })
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "pregnancy_care")
            put("chunk_id", id)
            put("complication_type", id.removePrefix("complication_"))
        }.toString()
    }

    private fun createDeliveryChunk(procedure: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", procedure.optString("title", "Delivery Procedure"))
            put("content", procedure.optString("description", ""))
            put("key_points", JSONArray().apply {
                val keySteps = procedure.optString("key_steps", "")
                if (keySteps.isNotEmpty()) {
                    put(keySteps)
                }
                val pphManagement = procedure.optString("pph_management", "")
                if (pphManagement.isNotEmpty()) {
                    put(pphManagement)
                }
                val eclampsiaManagement = procedure.optString("eclampsia_management", "")
                if (eclampsiaManagement.isNotEmpty()) {
                    put(eclampsiaManagement)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "delivery_care")
            put("chunk_id", id)
            put("procedure_type", "normal_delivery")
        }.toString()
    }

    private fun createPostpartumChunk(care: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", care.optString("title", "Postpartum Care"))
            put("content", care.optString("description", ""))
            put("key_points", JSONArray().apply {
                val keyAspects = care.optString("key_aspects", "")
                if (keyAspects.isNotEmpty()) {
                    put(keyAspects)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "postpartum_care")
            put("chunk_id", id)
            put("care_type", "immediate")
        }.toString()
    }

    private fun createNewbornChunk(care: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", care.optString("title", "Newborn Care"))
            put("content", care.optString("description", ""))
            put("key_points", JSONArray().apply {
                val keyPractices = care.optString("key_practices", "")
                if (keyPractices.isNotEmpty()) {
                    put(keyPractices)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "newborn_care")
            put("chunk_id", id)
            put("care_type", "essential")
        }.toString()
    }

    private fun createImmunizationChunk(item: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", item.optString("title", "Immunization"))
            put("content", item.optString("description", ""))
            put("key_points", JSONArray())
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "immunization")
            put("chunk_id", id)
            put("immunization_type", "vaccine")
        }.toString()
    }

    private fun createNutritionChunk(guideline: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", guideline.optString("title", "Nutrition Guideline"))
            put("content", guideline.optString("description", ""))
            put("key_points", JSONArray().apply {
                val dietaryAdvice = guideline.optString("dietary_advice", "")
                if (dietaryAdvice.isNotEmpty()) {
                    put(dietaryAdvice)
                }
            })
            put("guidelines", JSONArray().apply {
                val procedures = guideline.optString("procedures", "")
                if (procedures.isNotEmpty()) {
                    put(procedures)
                }
            })
            put("procedures", JSONArray())
            put("warnings", JSONArray().apply {
                val taboosMisconceptions = guideline.optString("taboos_misconceptions", "")
                if (taboosMisconceptions.isNotEmpty()) {
                    put(taboosMisconceptions)
                }
            })
            put("section_type", "nutrition")
            put("chunk_id", id)
            put("nutrition_type", "pregnancy")
        }.toString()
    }

    private fun createEmergencyChunk(emergency: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", emergency.optString("title", "Emergency Care"))
            put("content", emergency.optString("description", ""))
            put("key_points", JSONArray())
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "emergency_care")
            put("chunk_id", id)
            put("emergency_type", "severe_bleeding")
        }.toString()
    }

    private fun createGeneralGuidelineChunk(guideline: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", guideline.optString("title", "General Guideline"))
            put("content", guideline.optString("description", ""))
            put("key_points", JSONArray().apply {
                val programsInvolved = guideline.optString("programs_involved", "")
                if (programsInvolved.isNotEmpty()) {
                    put(programsInvolved)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "general_guidelines")
            put("chunk_id", id)
            put("category", "asha_responsibilities")
        }.toString()
    }

    private fun createGeneralPregnancyChunk(care: JSONObject): String {
        return JSONObject().apply {
            put("title", care.optString("title", "General Pregnancy Care"))
            put("content", care.optString("description", ""))
            put("key_points", JSONArray().apply {
                val keyAspects = care.optString("key_aspects", "")
                if (keyAspects.isNotEmpty()) {
                    put(keyAspects)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "pregnancy_care")
            put("chunk_id", "general_pregnancy_care")
        }.toString()
    }

    private fun createAbortionCareChunk(care: JSONObject): String {
        return JSONObject().apply {
            put("title", care.optString("title", "Comprehensive Abortion Care"))
            put("content", care.optString("description", ""))
            put("key_points", JSONArray())
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "pregnancy_care")
            put("chunk_id", "abortion_care")
        }.toString()
    }

    private fun createPregnancySchemeChunk(scheme: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", scheme.optString("title", "Pregnancy Scheme"))
            put("content", scheme.optString("description", ""))
            put("key_points", JSONArray().apply {
                val keyBenefits = scheme.optString("key_benefits", "")
                if (keyBenefits.isNotEmpty()) {
                    put(keyBenefits)
                }
                val keyInterventions = scheme.optString("key_interventions", "")
                if (keyInterventions.isNotEmpty()) {
                    put(keyInterventions)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "pregnancy_scheme")
            put("chunk_id", id)
            // Extract scheme type from id, e.g., "scheme_maternal_schemes_jssk" -> "maternal_schemes"
            val schemeType = id.removePrefix("scheme_").substringBefore("_")
            put("scheme_type", schemeType)
        }.toString()
    }

    private fun createFinancialSupportSchemeChunk(scheme: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", scheme.optString("title", "Financial Support Scheme"))
            put("content", scheme.optString("description", ""))
            put("key_points", JSONArray())
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "financial_support_scheme")
            put("chunk_id", id)
        }.toString()
    }

    private fun createClinicalProtocolChunk(protocol: JSONObject, id: String): String {
        return JSONObject().apply {
            put("title", protocol.optString("title", "Clinical Protocol"))
            put("content", protocol.optString("description", ""))
            put("key_points", JSONArray().apply {
                val labTests = protocol.optString("lab_tests", "")
                if (labTests.isNotEmpty()) {
                    put(labTests)
                }
                val supplementation = protocol.optString("supplementation", "")
                if (supplementation.isNotEmpty()) {
                    put(supplementation)
                }
                val commonIssuesManagement = protocol.optString("common_issues_management", "")
                if (commonIssuesManagement.isNotEmpty()) {
                    put(commonIssuesManagement)
                }
                val counseling = protocol.optString("counseling", "")
                if (counseling.isNotEmpty()) {
                    put(counseling)
                }
                val monitoring = protocol.optString("monitoring", "")
                if (monitoring.isNotEmpty()) {
                    put(monitoring)
                }
                val supportAndPractices = protocol.optString("support_and_practices", "")
                if (supportAndPractices.isNotEmpty()) {
                    put(supportAndPractices)
                }
                val cordManagement = protocol.optString("cord_management", "")
                if (cordManagement.isNotEmpty()) {
                    put(cordManagement)
                }
                val activeThirdStage = protocol.optString("active_third_stage", "")
                if (activeThirdStage.isNotEmpty()) {
                    put(activeThirdStage)
                }
                val essentialNewbornCare = protocol.optString("essential_newborn_care", "")
                if (essentialNewbornCare.isNotEmpty()) {
                    put(essentialNewbornCare)
                }
                val maternalDangerSigns = protocol.optString("maternal_danger_signs", "")
                if (maternalDangerSigns.isNotEmpty()) {
                    put(maternalDangerSigns)
                }
                val dangerSigns = protocol.optString("danger_signs", "")
                if (dangerSigns.isNotEmpty()) {
                    put(dangerSigns)
                }
                val lowBirthWeightCare = protocol.optString("low_birth_weight_care", "")
                if (lowBirthWeightCare.isNotEmpty()) {
                    put(lowBirthWeightCare)
                }
                val infectionManagement = protocol.optString("infection_management", "")
                if (infectionManagement.isNotEmpty()) {
                    put(infectionManagement)
                }
                val birthAsphyxiaResuscitation = protocol.optString("birth_asphyxia_resuscitation", "")
                if (birthAsphyxiaResuscitation.isNotEmpty()) {
                    put(birthAsphyxiaResuscitation)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "clinical_protocol")
            put("chunk_id", id)
            put("protocol_type", "comprehensive")
        }.toString()
    }

    private fun createHealthcareInfrastructureChunk(infrastructure: JSONObject): String {
        return JSONObject().apply {
            put("title", infrastructure.optString("title", "Healthcare Infrastructure"))
            put("content", infrastructure.optString("description", ""))
            put("key_points", JSONArray().apply {
                val caseStudy = infrastructure.optJSONObject("case_study_chennai")
                if (caseStudy != null) {
                    val caseStudyTitle = caseStudy.optString("title", "")
                    val caseStudyDescription = caseStudy.optString("description", "")
                    val stateSpecificSchemes = caseStudy.optString("state_specific_schemes", "")
                    val emergencyMedicalServices = caseStudy.optString("emergency_medical_services", "")
                    
                    if (caseStudyTitle.isNotEmpty()) put(caseStudyTitle)
                    if (caseStudyDescription.isNotEmpty()) put(caseStudyDescription)
                    if (stateSpecificSchemes.isNotEmpty()) put(stateSpecificSchemes)
                    if (emergencyMedicalServices.isNotEmpty()) put(emergencyMedicalServices)
                }
            })
            put("guidelines", JSONArray())
            put("procedures", JSONArray())
            put("warnings", JSONArray())
            put("section_type", "healthcare_infrastructure")
            put("chunk_id", "healthcare_infrastructure")
        }.toString()
    }

    // Helper to split oversized chunks based on character count (hard 1000 char limit)
    private fun addChunkWithSizeLimit(chunk: String, chunks: MutableList<String>, maxChars: Int = 1000) {
        val obj = JSONObject(chunk)
        val content = obj.optString("content", "")
        if (content.length <= maxChars) {
            chunks.add(chunk)
        } else {
            var start = 0
            var partIdx = 1
            while (start < content.length) {
                val end = minOf(start + maxChars, content.length)
                val partContent = content.substring(start, end)
                val partObj = JSONObject(obj.toString())
                partObj.put("content", partContent)
                partObj.put("chunk_id", obj.optString("chunk_id") + "_part$partIdx")
                chunks.add(partObj.toString())
                start = end
                partIdx++
            }
            Log.w(TAG, "Chunk too large, split into ${partIdx - 1} parts: ${obj.optString("title")}")
        }
    }
} 