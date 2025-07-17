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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.VolumeUp
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.littleb01s.ashasakhichat.presentation.screens.extractYouTubeVideoId

@Composable
fun CourseDetailScreen(
    courseId: String?,
    navController: NavController,
    viewModel: TrainingViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null // Add optional back callback
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

    // Flatten subContent and quiz into a single list for navigation
    val sectionList = remember(course, completedSubContent, quizAnswers) {
        val list = mutableListOf<SectionItem>()
        course.subContent?.forEach { list.add(SectionItem.SubContentSection(it)) }
        course.quiz?.forEach { list.add(SectionItem.QuizSection(it)) }
        list
    }
    val sectionCount = sectionList.size
    var currentSectionIndex by rememberSaveable { mutableStateOf(0) }

    // Completion logic
    val allSubContentComplete = course.subContent?.all { completedSubContent.contains(it.id) } != false
    val allQuizCorrect = course.quiz?.all { quizAnswers[it.id] == it.correctIndex } != false
    val allComplete = allSubContentComplete && allQuizCorrect

    // Use DetailScaffold for consistent back button and title
    com.littleb01s.ashasakhichat.presentation.DetailScaffold(
        title = course.title,
        onNavigateBack =  { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = if (sectionCount > 0) (currentSectionIndex + 1) / sectionCount.toFloat() else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF84D5B1)
                )
                Text(
                    text = "Section ${currentSectionIndex + 1} of $sectionCount",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Main Content
                val section = sectionList.getOrNull(currentSectionIndex)
                if (section != null) {
                    when (section) {
                        is SectionItem.SubContentSection -> {
                            val sub = section.subContent
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                                    val youTubeId = extractYouTubeVideoId(sub.mediaUrl)
                                    if (youTubeId != null) {
                                        // Use YouTubePlayerView for YouTube videos
                                        AndroidView(
                                            factory = {
                                                YouTubePlayerView(context).apply {
                                                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                                        override fun onReady(youTubePlayer: YouTubePlayer) {
                                                            youTubePlayer.loadVideo(youTubeId, 0f)
                                                        }
                                                    })
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        )
                                    } else {
                                        // Use ExoPlayer for direct video links
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
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = sub.text,
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(onClick = { chatViewModel.speak(sub.text) }) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Hear audio",
                                            tint = Color(0xFF432C81)
                                        )
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
                        is SectionItem.QuizSection -> {
                            val quiz = section.quiz
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
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
                }
            }
            // Navigation and completion buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { if (currentSectionIndex > 0) currentSectionIndex-- },
                        enabled = currentSectionIndex > 0
                    ) { Text("Previous") }
                    Button(
                        onClick = { if (currentSectionIndex < sectionCount - 1) currentSectionIndex++ },
                        enabled = currentSectionIndex < sectionCount - 1
                    ) { Text("Next") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    enabled = allComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allComplete) Color(0xFF84D5B1) else Color.LightGray
                    )
                ) {
                    Text(
                        if (allComplete) "Course Completed" else "Complete all sections and quiz to finish course",
                        color = if (allComplete) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}

// Helper sealed class for section list
sealed class SectionItem {
    data class SubContentSection(val subContent: com.littleb01s.ashasakhichat.presentation.screens.SubContent) : SectionItem()
    data class QuizSection(val quiz: com.littleb01s.ashasakhichat.presentation.screens.Quiz) : SectionItem()
}

@Composable
fun SidebarItem(title: String, isSelected: Boolean, isCompleted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                if (isSelected) Color(0xFFE0E0FF) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF432C81) else Color.LightGray,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(18.dp)
            )
        }
    }
} 