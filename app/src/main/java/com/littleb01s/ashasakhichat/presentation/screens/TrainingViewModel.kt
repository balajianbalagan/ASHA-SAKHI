package com.littleb01s.ashasakhichat.presentation.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

data class QuizOption(
    val text: String,
    val image: String? = null
)
data class Quiz(
    val id: String,
    val question: String,
    val options: List<QuizOption>,
    val correctIndex: Int
)

data class SubContent(
    val id: String,
    val type: String, // "photo" or "video"
    val mediaUrl: String,
    val text: String,
    val completed: Boolean
)

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "photo" or "video"
    val mediaUrl: String,
    val completed: Boolean,
    val subContent: List<SubContent>? = null,
    val quiz: List<Quiz>? = null
)

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val app: Application,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _completedSubContent = MutableStateFlow<Set<String>>(emptySet())
    val completedSubContent: StateFlow<Set<String>> = _completedSubContent.asStateFlow()

    private val _quizAnswers = MutableStateFlow<Map<String, Int>>(emptyMap()) // quizId -> selectedIndex
    val quizAnswers: StateFlow<Map<String, Int>> = _quizAnswers.asStateFlow()

    private val COMPLETED_SUBCONTENT_KEY = "asha_training_completed_subcontent"
    private val QUIZ_ANSWERS_KEY = "asha_training_quiz_answers"

    init {
        loadCourses()
        loadCompletedSubContent()
        loadQuizAnswers()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            val assetManager = app.assets
            val inputStream = assetManager.open("asha_training_courses.json")
            val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val loadedCourses = mutableListOf<Course>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val subContentList = if (obj.has("subContent")) {
                    val subArray = obj.getJSONArray("subContent")
                    (0 until subArray.length()).map { j ->
                        val subObj = subArray.getJSONObject(j)
                        SubContent(
                            id = subObj.getString("id"),
                            type = subObj.getString("type"),
                            mediaUrl = subObj.getString("mediaUrl"),
                            text = subObj.getString("text"),
                            completed = subObj.getBoolean("completed")
                        )
                    }
                } else null
                val quizList = if (obj.has("quiz")) {
                    val quizArray = obj.getJSONArray("quiz")
                    (0 until quizArray.length()).map { k ->
                        val quizObj = quizArray.getJSONObject(k)
                        val optionsArray = quizObj.getJSONArray("options")
                        val options = (0 until optionsArray.length()).map { l ->
                            val optObj = optionsArray.getJSONObject(l)
                            QuizOption(
                                text = optObj.getString("text"),
                                image = optObj.optString("image", null)
                            )
                        }
                        Quiz(
                            id = quizObj.getString("id"),
                            question = quizObj.getString("question"),
                            options = options,
                            correctIndex = quizObj.getInt("correctIndex")
                        )
                    }
                } else null
                loadedCourses.add(
                    Course(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        type = obj.getString("type"),
                        mediaUrl = obj.getString("mediaUrl"),
                        completed = obj.getBoolean("completed"),
                        subContent = subContentList,
                        quiz = quizList
                    )
                )
            }
            _courses.value = loadedCourses
        }
    }

    private fun loadCompletedSubContent() {
        val completedSet = preferencesManager.prefs.getStringSet(COMPLETED_SUBCONTENT_KEY, emptySet()) ?: emptySet()
        _completedSubContent.value = completedSet
    }

    private fun loadQuizAnswers() {
        val json = preferencesManager.prefs.getString(QUIZ_ANSWERS_KEY, null)
        if (json != null) {
            val map = mutableMapOf<String, Int>()
            val obj = org.json.JSONObject(json)
            for (key in obj.keys()) {
                map[key] = obj.getInt(key)
            }
            _quizAnswers.value = map
        }
    }

    fun toggleSubContentCompletion(subContentId: String) {
        val current = _completedSubContent.value.toMutableSet()
        if (current.contains(subContentId)) {
            current.remove(subContentId)
        } else {
            current.add(subContentId)
        }
        _completedSubContent.value = current
        preferencesManager.prefs.edit().putStringSet(COMPLETED_SUBCONTENT_KEY, current).apply()
    }

    fun answerQuiz(quizId: String, selectedIndex: Int) {
        val updated = _quizAnswers.value.toMutableMap()
        updated[quizId] = selectedIndex
        _quizAnswers.value = updated
        // Persist as JSON
        val json = org.json.JSONObject(updated as Map<*, *>).toString()
        preferencesManager.prefs.edit().putString(QUIZ_ANSWERS_KEY, json).apply()
    }

    fun isCourseComplete(course: Course): Boolean {
        val subContent = course.subContent ?: return false
        val quiz = course.quiz
        val allSubContentComplete = subContent.all { _completedSubContent.value.contains(it.id) }
        val allQuizCorrect = quiz?.all { _quizAnswers.value[it.id] == it.correctIndex } ?: true
        return allSubContentComplete && allQuizCorrect
    }
} 