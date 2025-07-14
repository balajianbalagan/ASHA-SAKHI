package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import coil.compose.rememberAsyncImagePainter
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import androidx.compose.runtime.DisposableEffect
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import com.littleb01s.ashasakhichat.presentation.ChatViewModel

@Composable
fun CourseDetailScreen(
    courseId: String?,
    navController: NavController,
    viewModel: TrainingViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val completedSubContent by viewModel.completedSubContent.collectAsState()
    val quizAnswers by viewModel.quizAnswers.collectAsState()
    val course = courses.find { it.id == courseId }
    val scrollState = rememberScrollState()

    if (course == null) {
        Text("Course not found", color = Color.Red)
        return
    }

    DetailScaffold(
        title = course.title,
        onNavigateBack = { navController.navigateUp() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            course.subContent?.forEach { sub ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (sub.type == "photo") {
                            Image(
                                painter = rememberAsyncImagePainter(model = sub.mediaUrl),
                                contentDescription = sub.text,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else if (sub.type == "video") {
                            val context = LocalContext.current
                            val exoPlayer = remember {
                                ExoPlayer.Builder(context).build().apply {
                                    val mediaItem = MediaItem.fromUri(sub.mediaUrl)
                                    setMediaItem(mediaItem)
                                    prepare()
                                    playWhenReady = false
                                }
                            }
                            DisposableEffect(Unit) {
                                onDispose { exoPlayer.release() }
                            }
                            AndroidView(
                                factory = {
                                    PlayerView(context).apply {
                                        player = exoPlayer
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            600
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sub.text,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = { chatViewModel.speak(sub.text) }) {
                                Text("Start")
                            }
                            Button(
                                onClick = { viewModel.toggleSubContentCompletion(sub.id) },
                                enabled = !completedSubContent.contains(sub.id)
                            ) {
                                Text(if (completedSubContent.contains(sub.id)) "Completed" else "Mark as Complete")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Quiz section
            course.quiz?.let { quizList ->
                Text("Quiz", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                quizList.forEach { quiz ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(quiz.question, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            quiz.options.forEachIndexed { idx, opt ->
                                val selected = quizAnswers[quiz.id] == idx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.answerQuiz(quiz.id, idx) }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (opt.image != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = opt.image),
                                            contentDescription = opt.text,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(opt.text, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = if (idx == quiz.correctIndex) Color(0xFF4CAF50) else Color.Red
                                        )
                                    }
                                }
                            }
                            // Feedback
                            val selectedIdx = quizAnswers[quiz.id]
                            if (selectedIdx != null) {
                                if (selectedIdx == quiz.correctIndex) {
                                    Text("Correct!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Incorrect", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                val allQuizCorrect = quizList.all { quizAnswers[it.id] == it.correctIndex }
                if (allQuizCorrect) {
                    Text("All quiz answers are correct!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val allComplete = (course.subContent?.all { completedSubContent.contains(it.id) } != false) && (course.quiz?.all { quizAnswers[it.id] == it.correctIndex } != false)
            Button(
                onClick = {},
                enabled = allComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (allComplete) "Course Completed" else "Complete all sections and quiz to finish course")
            }
        }
    }
} 