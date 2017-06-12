package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.subtasks.strings.Replace;
import org.apache.tools.ant.BuildException;

/**
 * Task para manipulacao de strings em builds
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class StringsTask extends Task {

    // adiciona comando de replace
    public void addReplace(Replace replace) {
        addTask(replace);
    }

    public void validate() {

    }

    @Override
    public void execute() throws BuildException {

        for (Command command : getCommands()) {
            command.execute();
        }

        for (Task task : getTasks()) {
            task.execute();
        }
    }
}
