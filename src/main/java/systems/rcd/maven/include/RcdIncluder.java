package systems.rcd.maven.include;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RcdIncluder
{
    private static final Pattern PATTERN = Pattern.compile( "include\\s*\\(\\s*['\"]([^'^\"]+)['\"]\\s*\\)\\s*;" );

    private Path source;

    private Path target;

    private boolean deleteSource;

    private Set<Path> processedPath = new HashSet<>();

    private RcdIncluder( final Builder builder )
    {
        source = builder.source;
        target = builder.target;
        deleteSource = builder.deleteSource;
    }

    public void execute()
        throws IOException
    {
        final File targetFile = target.toFile();
        if ( !targetFile.exists() )
        {
            targetFile.getParentFile().mkdirs();
            targetFile.createNewFile();
        }
        try (BufferedWriter bw = Files.newBufferedWriter( target ))
        {
            processFile( source, bw );
        }
    }

    private void processFile( final Path filePath, final BufferedWriter bw )
        throws IOException
    {
        final Path absolutePath = filePath.toAbsolutePath();
        final boolean alreadyProcessed = !processedPath.add( absolutePath );
        if ( alreadyProcessed )
        {
            return;
        }

        try (BufferedReader br = Files.newBufferedReader( filePath ))
        {
            String line = br.readLine();
            while ( line != null )
            {
                processLine( filePath, line, bw );
                line = br.readLine();
                if ( line != null )
                {
                    bw.newLine();
                }
            }
        }
        if ( deleteSource )
        {
            Files.delete( filePath );
        }
    }

    private void processLine( final Path filePath, final String line, final BufferedWriter bw )
        throws IOException
    {
        final Matcher matcher = PATTERN.matcher( line );
        boolean found = matcher.find();
        if ( !found )
        {
            bw.write( line );
            return;
        }

        final StringBuffer buffer = new StringBuffer();
        while ( found )
        {
            matcher.appendReplacement( buffer, "" );
            bw.write( buffer.toString() );
            buffer.setLength( 0 );

            final String referencedFileString = matcher.group( 1 );
            final Path referencedFilePath = filePath.resolveSibling( referencedFileString );

            processFile( referencedFilePath, bw );

            found = matcher.find();
        }

        matcher.appendTail( buffer );
        bw.write( buffer.toString() );
    }


    public static Builder create()
    {
        return new Builder();
    }


    public static final class Builder
    {
        private Path source;

        private Path target;

        private boolean deleteSource;

        private Builder()
        {
        }

        public Builder source( final Path source )
        {
            this.source = source;
            return this;
        }

        public Builder target( final Path target )
        {
            this.target = target;
            return this;
        }

        public Builder deleteSource( final boolean deleteSource )
        {
            this.deleteSource = deleteSource;
            return this;
        }

        public RcdIncluder build()
        {
            return new RcdIncluder( this );
        }
    }
}
