package net.kernelits.antlibtasks.utils;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Echo;
import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.tasks.Task;

/**
 * Classe de suporte para Log
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class AntLog {

    // variaveis privadas da classe
    private Task task = null;
    private Command command = null;
    private Echo empty = null;

    // construtor para task
    public AntLog(Task task) {
        this.task = task;
        this.command = null;
    }

    // construtor para comando
    public AntLog(Command command) {
        this.task = null;
        this.command = command;
    }

    // metodo interno de envio para o log
    private void send(String message, int level) {

        // classe do comando
        String classname = "";

        // task que sera utlizada
        Task mytask;

        // no caso de existir o comando,
        // busca o nome do comando
        if (command != null)
        {
            // obtem o ultimo nome da classe
            String[] fullname = command.getClass().getName().split("\\.");
            classname = fullname[fullname.length - 1];

            // converte para lowercase
            classname = classname.toLowerCase();

            // concatena com o separador de nome
            classname = "<" + classname + "> ";

            // define mytask como a task do comando
            mytask = command.getTask();

        } else {

            // define mytask pela task informada
            mytask = task;
        }

        // verifica se foi informado alguma mensagem
        if (!"".equals(message)) {

            // monta a mensagem com o comando caso exista
            String msg = classname + message;

            // envia para o log da task
            mytask.log(msg, level);

        } else {

            if (empty == null) {

                // cria a task echo com mensagem vazia
                empty = (Echo)mytask.getProject().createTask("echo");
                empty.setTaskName(mytask.getTaskName());
                empty.addText("");
            }

            // neste caso envia uma mensagem vazia
            empty.execute();
        }
    }

    // log em nivel de info
    public void info(String message) {
        send(message, Project.MSG_INFO);
    }

    // log em nivel verbose
    public void verbose(String message) {
        send(message, Project.MSG_VERBOSE);
    }

    // log em nivel de warning
    public void warning(String message) {
        send(message, Project.MSG_WARN);
    }

    // log em nivel de error
    public void error(String message) {
        send(message, Project.MSG_ERR);
    }
}
