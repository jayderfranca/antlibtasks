package net.kernelits.antlibtasks.deploy;

import net.kernelits.antlibtasks.pwd.Password;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.util.List;

/**
 * Classe de deploy do Weblogic
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class WLDeploy {

    // variaveis
    private Project project;
    private Task task;
    private String url;
    private String targets;
    private String username;
    private String password;
    private String contextName;
    private File jvm;
    private Reference classpathRefId;
    private List<Path> classpath;
    boolean printCommand = false;

    public WLDeploy(Project project, Task task, String url, String targets, String username, String password,
                    String contextName, File jvm, Reference classpathRefId,
                    List<Path> classpath, boolean printCommand) throws Exception {

        // passa para a classe as propriedades
        // necessarias para a task
        this.project = project;
        this.task = task;
        this.url = url;
        this.targets = targets;
        this.username = username;
        this.contextName = contextName;
        this.jvm = jvm;
        this.classpathRefId = classpathRefId;
        this.classpath = classpath;
        this.password = Password.getPasswordText(password);
        this.printCommand = printCommand;
    }

    private int execJavaTask(boolean failOnError, int maxMemory, String args) {

        // retorno da execucao do comando
        int retJvm = 0;

        // cria uma nova task java
        Java java = (Java) project.createTask("java");

        // define o nome da task owner
        java.setTaskName(task.getTaskName());

        // define o nome da classe main
        java.setClassname("weblogic.Deployer");

        // define a execucao da jvm fora da jvm corrente
        java.setFork(true);

        // verifica se foi informando uma
        // jvm especifica
        if (!(jvm == null)) {

            // jvm separada
            java.setJvm(jvm.getAbsolutePath());

        } else {

            // cria um clone da jvm atual
            java.setCloneVm(true);
        }

        // argumentos da jvm de memoria
        java.createJvmarg().setLine("-Xms" + maxMemory + "m -Xmx" + maxMemory + "m");

        // define o classpath da jvm
        java.setClasspathRef(classpathRefId);
        for (Path path : classpath) {
            java.createClasspath().setPath(path.toString());
        }

        // cria os argumentos informados
        java.createArg().setLine(args);

        // verifica se somente eh para
        // exibir o comando
        if (printCommand)
            System.out.println(java.getCommandLine());
        else
            // executa o comando
            retJvm = java.executeJava();

        // retorno
        return (failOnError ? retJvm : 0);
    }

    public int stop(boolean failOnError, int maxMemory) {

        // comando de stop do contexto

        // cria a linha de argumentos para a classe de deploy
        String argLine = " -adminurl " + url +
                " -username " + username +
                " -password " + password +
                " -stop" +
                " -name " + contextName +
                ("".equals(targets) ? "" : " -targets " + targets);

        // executa o comando java
        return execJavaTask(failOnError, maxMemory, argLine);
    }

    public int stop() {
        return stop(false, 0);
    }

    public int stop(boolean failOnError) {
        return stop(failOnError, 0);
    }

    public int stop(int maxMemory) {
        return stop(false, maxMemory);
    }

    public int undeploy(boolean failOnError, int maxMemory) {

        // comando de undeploy do contexto

        // cria a linha de argumentos para a classe de deploy
        String argLine = " -adminurl " + url +
                " -username " + username +
                " -password " + password +
                " -usenonexclusivelock" +
                " -undeploy" +
                " -name " + contextName +
                ("".equals(targets) ? "" : " -targets " + targets);

        // executa o comando java
        return execJavaTask(failOnError, maxMemory, argLine);
    }

    public int undeploy() {
        return undeploy(false, 0);
    }

    public int undeploy(boolean failOnError) {
        return undeploy(failOnError, 0);
    }

    public int undeploy(int maxMemory) {
        return undeploy(false, maxMemory);
    }

    public int deploy(boolean failOnError, int maxMemory, String source) {

        // comando de deploy do contexto

        // cria a linha de argumentos para a classe de deploy
        String argLine = " -adminurl " + url +
                " -username " + username +
                " -password " + password +
                " -remote" +
                " -usenonexclusivelock" +
                " -nostage" +
                " -deploy" +
                " -name " + contextName +
                " -source " + source +
                ("".equals(targets) ? "" : " -targets " + targets);

        // executa o comando java
        return execJavaTask(failOnError, maxMemory, argLine);
    }

    public int deploy(String source) {
        return deploy(false, 0, source);
    }

    public int deploy(boolean failOnError, String source) {
        return deploy(failOnError, 0, source);
    }

    public int deploy(int maxMemory, String source) {
        return deploy(false, maxMemory, source);
    }

    public int start(boolean failOnError, int maxMemory) {

        // comando de stop do contexto

        // cria a linha de argumentos para a classe de deploy
        String argLine = " -adminurl " + url +
                " -username " + username +
                " -password " + password +
                " -start" +
                " -name " + contextName +
                ("".equals(targets) ? "" : " -targets " + targets);

        // executa o comando java
        return execJavaTask(failOnError, maxMemory, argLine);
    }

    public int start() {
        return start(false, 0);
    }

    public int start(boolean failOnError) {
        return start(failOnError, 0);
    }

    public int start(int maxMemory) {
        return start(false, maxMemory);
    }
}