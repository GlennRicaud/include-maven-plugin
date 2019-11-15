package systems.rcd.maven.include;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "include")
public class IncludeMojo
    extends AbstractMojo
{
    @Parameter
    private File source;

    @Parameter
    private File target;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            RcdIncluder.create().
                source( source.toPath() ).
                target( target.toPath() ).
                build().
                execute();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Error while processing file [" + source + "]", e );
        }
    }
}