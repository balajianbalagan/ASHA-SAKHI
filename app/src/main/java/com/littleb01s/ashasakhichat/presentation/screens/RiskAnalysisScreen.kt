package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.littleb01s.ashasakhichat.presentation.navigation.Screen

@Composable
fun RiskAnalysisScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPregnancyRisk: () -> Unit
) {
    DetailScaffold(
        title = stringResource(R.string.risk_analysis),
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Risk Assessment Tools",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use these tools to assess patient risks and get recommendations",
                        fontSize = 14.sp
                    )
                }
            }

            // Risk assessment categories
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToPregnancyRisk
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pregnancy Risk Assessment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Assess pregnancy-related risks based on vital signs and medical history",
                        fontSize = 14.sp
                    )
                }
            }

            // Other risk categories (placeholder for now)
            repeat(4) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when(index) {
                                0 -> "Nutritional Risk Evaluation"
                                1 -> "Medical History Analysis"
                                2 -> "Environmental Risk Factors"
                                else -> "Social Support Assessment"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coming soon",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
} 