package com.littleb01s.ashasakhichat.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownRenderer(
    text: String,
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    val markdownText = parseMarkdown(text)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = markdownText,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

private fun parseMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        // Split the text into lines to handle headers and lists
        val lines = text.split("\n")
        
        for (i in lines.indices) {
            val line = lines[i]
            
            when {
                // Headers
                line.startsWith("# ") -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    ) {
                        append(line.substring(2))
                        append("\n")
                    }
                }
                line.startsWith("## ") -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    ) {
                        append(line.substring(3))
                        append("\n")
                    }
                }
                line.startsWith("### ") -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    ) {
                        append(line.substring(4))
                        append("\n")
                    }
                }
                
                // Bold text
                line.contains("**") -> {
                    val parts = line.split("**")
                    for (j in parts.indices) {
                        if (j % 2 == 0) {
                            append(parts[j])
                        } else {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(parts[j])
                            }
                        }
                    }
                    append("\n")
                }
                
                // Italic text
                line.contains("*") -> {
                    val parts = line.split("*")
                    for (j in parts.indices) {
                        if (j % 2 == 0) {
                            append(parts[j])
                        } else {
                            withStyle(
                                style = SpanStyle(
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append(parts[j])
                            }
                        }
                    }
                    append("\n")
                }
                
                // Underlined text
                line.contains("__") -> {
                    val parts = line.split("__")
                    for (j in parts.indices) {
                        if (j % 2 == 0) {
                            append(parts[j])
                        } else {
                            withStyle(
                                style = SpanStyle(
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(parts[j])
                            }
                        }
                    }
                    append("\n")
                }
                
                // Bullet points
                line.startsWith("- ") || line.startsWith("* ") -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("• ")
                        append(line.substring(2))
                        append("\n")
                    }
                }
                
                // Numbered lists
                line.matches(Regex("^\\d+\\. .*")) -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(line)
                        append("\n")
                    }
                }
                
                // Code blocks
                line.startsWith("```") -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    ) {
                        append(line.substring(3))
                        append("\n")
                    }
                }
                
                // Regular text
                else -> {
                    append(line)
                    append("\n")
                }
            }
        }
    }
} 