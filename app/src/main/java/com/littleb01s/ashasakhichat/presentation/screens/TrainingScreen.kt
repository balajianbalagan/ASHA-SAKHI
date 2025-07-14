package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import coil.compose.rememberAsyncImagePainter
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.screens.TrainingViewModel
import androidx.navigation.NavController

@Composable
fun TrainingScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val courses by viewModel.courses.collectAsState()
    val completedSubContent by viewModel.completedSubContent.collectAsState()
    val scrollState = rememberScrollState()

    DetailScaffold(
        title = stringResource(R.string.asha_training),
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall progress bar
            val completedCoursesCount = courses.count { viewModel.isCourseComplete(it) }
            if (courses.isNotEmpty()) {
                val progress = completedCoursesCount / courses.size.toFloat()
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF84D5B1)
                )
                Text(
                    text = "$completedCoursesCount of ${courses.size} courses completed",
                    color = Color(0xFF432C81),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            courses.forEach { course ->
                val isComplete = viewModel.isCourseComplete(course)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate("course_detail/${course.id}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = course.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (course.type == "photo") {
                            Image(
                                painter = rememberAsyncImagePainter(model = course.mediaUrl),
                                contentDescription = course.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else if (course.type == "video") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Placeholder for video
                                Text("[Video Placeholder]", color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 120.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = course.description,
                                fontSize = 14.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Status bar
                        LinearProgressIndicator(
                            progress = if (isComplete) 1f else 0f,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isComplete) Color(0xFF84D5B1) else Color.LightGray
                        )
                        Text(
                            text = if (isComplete) "Completed" else "Not Completed",
                            color = if (isComplete) Color(0xFF84D5B1) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
} 