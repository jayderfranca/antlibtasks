package net.kernelits.antlibtasks.ant.subtasks.strings;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.commands.replace.String;
import net.kernelits.antlibtasks.ant.tasks.Task;
import org.apache.tools.ant.BuildException;

public class Replace extends Task {

    // adiciona a task de replace
    public void addString(String string) {
        addCommand(string);
    }

    @Override
    public void execute() throws BuildException {

        for (Command command : getCommands()) {
            command.execute();
        }
    }
}
