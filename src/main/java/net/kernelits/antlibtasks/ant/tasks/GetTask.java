package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.utils.pwd.Password;
import org.apache.tools.ant.taskdefs.Get;

public class GetTask extends Get {

    public void setPasswordEncrypted(String p) {

        String password;

        try {
            // descriptografa a senha
            password = Password.getPasswordText(p);
        } catch (Exception ex) {

            // em caso de erro passa a senha informada
            password = p;
        }

        // passa a senha limpa para a task real
        super.setPassword(password);
    }
}
