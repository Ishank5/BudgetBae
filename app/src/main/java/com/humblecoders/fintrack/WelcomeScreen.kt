package com.humblecoders.fintrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeGold

@Composable
fun WelcomeScreen(
    onSignUpClick: () -> Unit,
    onLogInClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBaeDarkGreen
                    )
                    Text(
                        text = "BAE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBaeGold
                    )
                }
                
                Text(
                    text = "Your Money Wingmate",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Welcome to BudgetBae",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BudgetBaeDarkGreen
                )
                
                Text(
                    text = "Your smart, friendly finance\nsidekick",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BudgetBaeDarkGreen
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onLogInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BudgetBaeDarkGreen
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Log In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
