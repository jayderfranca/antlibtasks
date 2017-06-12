package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.commands.file.BlockRemove;
import org.apache.tools.ant.BuildException;

import java.io.File;

/**
 * Task para operacoes de arquivos
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class FileTask extends Task {

    // variaveis
    private File in;
    private File out;

    // sets
    public void setIn(File in) {
        this.in = in;
    }

    public File getIn() {
        return this.in;
    }

    public void setOut(File out) {
        this.out = out;
    }

    public File getOut() {
        return (this.out == null ? this.in : this.out);
    }

    // adiciona comando de blockremove
    public void addBlockRemove(BlockRemove blockremove) {
        addCommand(blockremove);
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        for (Command command : getCommands()) {
            command.execute();
        }
    }
}
