package hu.unideb.inf.validator;

import java.util.HashMap;

import javax.xml.XMLConstants;

import javax.xml.transform.stream.StreamSource;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXParseException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class SchemaValidator {

	private static Options options = new Options();

	private static HashMap<String, String[]> schemaSettings = new HashMap<String, String[]>();

	static {
		schemaSettings.put("RNC",
			new String[] {
				XMLConstants.RELAXNG_NS_URI,
				"com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory"
			}
		);
		schemaSettings.put("RNG",
			new String[] {
				XMLConstants.RELAXNG_NS_URI,
				"com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory"
			}
		);
		schemaSettings.put("XSD",
			new String[] {
				XMLConstants.W3C_XML_SCHEMA_NS_URI,
				null	// use default
			}
		);
		schemaSettings.put("XSD11",
			new String[] {
				"http://www.w3.org/XML/XMLSchema/v1.1",
				"org.apache.xerces.jaxp.validation.XMLSchema11Factory"
			}
		);
	};

	static {
		options.addOption(
			OptionBuilder
				.withLongOpt("schemaLanguage")
				.hasArg()
				.withArgName("language")
				.withDescription("select schema language (RNC, RNG, XSD, XSD11)")
				.isRequired(false)
				.create("l")
		);
	}

	private static void printHelpAndExit(int status) {
		new HelpFormatter().printHelp("java " + SchemaValidator.class.getName() + " [schema] instance", options, true);
		System.exit(status);
	}

	public static void main(String[] args) {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelpAndExit(1);
		}
		args = cmd.getArgs();
		if (args.length != 1 && args.length != 2) {
			printHelpAndExit(1);
		}
		String schemaLanguage = cmd.getOptionValue("l");
		if (! schemaSettings.containsKey(schemaLanguage)) {
			System.err.printf("Unrecognized schema language: %s\n", schemaLanguage);
			printHelpAndExit(1);
		}
		if (! schemaLanguage.equals("XSD")) {
			System.setProperty("javax.xml.validation.SchemaFactory:" + schemaSettings.get(schemaLanguage)[0],
				schemaSettings.get(schemaLanguage)[1]);
		}
		try {
			SimpleErrorHandler handler = new SimpleErrorHandler(true);
			SchemaFactory sf = SchemaFactory.newInstance(schemaSettings.get(schemaLanguage)[0]);
			sf.setErrorHandler(handler);
			Schema schema = args.length == 1 ? sf.newSchema() : sf.newSchema(new StreamSource(args[0]));
			Validator validator = schema.newValidator();
			validator.setErrorHandler(handler);
			validator.validate(new StreamSource(args.length == 1 ? args[0] : args[1]));
		} catch(SAXParseException e) {
			System.exit(1);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

}
