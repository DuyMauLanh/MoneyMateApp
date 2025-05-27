package com.example.moneymate.service
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.example.moneymate.R

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class JavaMailAPI(
    private val context: Context,
    private val email: String,
    private val subject: String,
    private val message: String,
    private val callback: (Boolean) -> Unit
) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void?): Boolean? {
        try {
            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"

            val session = Session.getInstance(props,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        // Thay bằng email & mật khẩu ứng dụng Gmail của bạn
                        return PasswordAuthentication("nguyengiap11a1bx@gmail.com", "udodumbqhiggmevu")
                    }
                })

            val messageObj = MimeMessage(session)
            messageObj.setFrom(InternetAddress("nguyengiap11a1bx@gmail.com"))
            messageObj.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            messageObj.subject = subject
            messageObj.setText(message)

            Transport.send(messageObj)
            return true
        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (result) {
            Toast.makeText(context, context.getString(R.string.email_c_g_i), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context,
                context.getString(R.string.g_i_email_th_t_b_i), Toast.LENGTH_SHORT).show()
        }
        callback(result)
    }
}
