package net.kernelits.antlibtasks.utils.deploy;

import net.kernelits.antlibtasks.utils.pwd.Password;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Classe de deploy do JBoss
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class JBDeploy {

    // variaveis
    private Project project;
    private Task task;
    private String url;
    private String username;
    private String password;
    private List<String> filesUndeploy;
    private String fileDeploy;
    private File jvm;
    private Reference classpathRefId;
    private List<Path> classpath;
    boolean printCommand = false;

    public JBDeploy(Project project, Task task, String url, String username, String password,
                    List<String> filesUndeploy, String fileDeploy, File jvm, Reference classpathRefId,
                    List<Path> classpath, boolean printCommand) throws Exception {

        // passa para a classe as propriedades
        // necessarias para a task
        this.project = project;
        this.task = task;
        this.url = url;
        this.username = username;
        this.jvm = jvm;
        this.classpathRefId = classpathRefId;
        this.classpath = classpath;
        this.password = Password.getPasswordText(password);
        this.filesUndeploy = filesUndeploy;
        this.fileDeploy = fileDeploy;
        this.printCommand = printCommand;
    }

    private String getDefaultArguments() {

        // cria argumentos default para chamada do comando
        StringBuilder sb = new StringBuilder();

        // url do servidor
        sb.append(" --server='" + url + "'");

        // verifica se foi informado o usuario
        // caso tenha sido, acrescenta usuario e senha no comando
        if (!"".equals(username.trim())) {

            sb.append(" --user=" + username);
            sb.append(" --password=" + password);
        }

        // retorno dos argumentos
        return sb.toString();
    }

    private int execJavaTask(boolean failOnError, int maxMemory, String args) {

        // retorno da execucao do comando
        int retJvm = 0;

        // cria uma nova task java
        Java java = (Java) project.createTask("java");

        // define o nome da task owner
        java.setTaskName(task.getTaskName());

        // define o nome da classe main
        java.setClassname("org.jboss.console.twiddle.Twiddle");

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

        // redireciona a saida para uma propriedade
        java.setOutputproperty("twiddle.command.output");

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

    public int undeploy(boolean failOnError, int maxMemory) throws MalformedURLException {

        // retorno geral da funcao
        int exit = 0;

        // comando de undeploy dos arquivos antigos

        for (String fileUndeploy : this.filesUndeploy) {

            // monta a url para undeploy
            URL undeploy = new URL("file://" + fileUndeploy);

            // cria a linha de argumentos para a classe de deploy/undeploy
            String argLine = getDefaultArguments() +
                    " invoke" +
                    " 'jboss.system:service=MainDeployer'" +
                    " undeploy" +
                    " '" + undeploy.toString() + "'";

            // executa o comando java
            int code = execJavaTask(failOnError, maxMemory, argLine);

            // verifica se o comando possui erro
            if (code != 0)
                exit = code;
        }

        // finaliza a funcao
        return exit;
    }

    public int undeploy() throws MalformedURLException {
        return undeploy(false, 0);
    }

    public int undeploy(boolean failOnError) throws MalformedURLException {
        return undeploy(failOnError, 0);
    }

    public int undeploy(int maxMemory) throws MalformedURLException {
        return undeploy(false, maxMemory);
    }

    public int deploy(boolean failOnError, int maxMemory, String source) throws MalformedURLException {

        // comando de deploy do arquivo novo

        // monta a url para deploy
        URL deploy = new URL("file://" + this.fileDeploy);

        // cria a linha de argumentos para a classe de deploy/undeploy
        String argLine = getDefaultArguments() +
                " invoke" +
                " 'jboss.system:service=MainDeployer'" +
                " deploy" +
                " '" + deploy.toString() + "'";

        // executa o comando java
        return execJavaTask(failOnError, maxMemory, argLine);
    }

    public int deploy(String source) throws MalformedURLException {
        return deploy(false, 0, source);
    }

    public int deploy(boolean failOnError, String source) throws MalformedURLException {
        return deploy(failOnError, 0, source);
    }

    public int deploy(int maxMemory, String source) throws MalformedURLException {
        return deploy(false, maxMemory, source);
    }
}