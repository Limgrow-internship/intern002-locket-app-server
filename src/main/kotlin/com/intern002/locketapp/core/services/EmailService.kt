package com.intern002.locketapp.core.services

import org.apache.commons.mail.SimpleEmail

class EmailService(
    private val host: String,
    private val port: Int,
    private val user: String,
    private val pass: String
) {
    fun sendResetPasswordEmail(toEmail: String, code: String) {
        try {
            val email = SimpleEmail()
            email.hostName = host
            email.setSmtpPort(port)
            email.setAuthentication(user, pass)
            email.isStartTLSEnabled = true

            email.setFrom(user, "Locket App Support")
            email.subject = "Reset Your Password"
            email.setMsg("Your verification code is: $code\n\nValid for 5 minutes.")
            email.addTo(toEmail)

            email.send()
            println("✅ Email sent to $toEmail")

        } catch (e: Exception) {
            println("❌ Email error: ${e.message}")
            e.printStackTrace()
        }
    }
}