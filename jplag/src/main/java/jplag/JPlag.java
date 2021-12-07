package jplag;


import jplag.options.CommandLineOptions;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JPlag {
	public static void main(String[] args) {

        final Logger LOGGER = Logger.getLogger(Report.class.getName());

		if (args.length == 0)
			CommandLineOptions.usage();
		else {
            try {
                CommandLineOptions options = new CommandLineOptions(args, null);
                Program program = new Program(options);

                System.out.println("initialize ok");
                program.run();
            }
            catch(ExitException ex) {
                String e = "Error: "+ex.getReport();
                LOGGER.log(Level.SEVERE, "{0}", e);
                System.exit(1);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception occur in main", e);
            }
        }
	}
}
