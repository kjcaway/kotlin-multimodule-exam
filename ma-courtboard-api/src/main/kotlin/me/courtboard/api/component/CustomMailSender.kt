package me.courtboard.api.component

import jakarta.mail.internet.MimeMessage
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.HtmlUtil
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.http.HttpStatus
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component


@Component
class CustomMailSender(
    private val javaMailSender: JavaMailSender
) {
    private final val mailTemplateFilePath = "mail/signup.html"

    fun sendMimeMessage(subject: String, to: String, args: Map<String, String>) {
        val mimeMessage: MimeMessage = javaMailSender.createMimeMessage()

        try {
            val mimeMessageHelper = MimeMessageHelper(mimeMessage, false, "UTF-8")
            mimeMessageHelper.setTo(to)
            mimeMessageHelper.setSubject(subject)

            val content = HtmlUtil.createHtmlBody(mailTemplateFilePath, args)

            mimeMessageHelper.setText(content, true)

            javaMailSender.send(mimeMessage)
        } catch (e: Exception) {
            log.error("failed to send mail", e)
            throw CustomRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to send mail")
        }
    }
}