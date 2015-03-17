package edu.arizona.biosemantics.micropie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.micropie.classify.ILabel;
import edu.arizona.biosemantics.micropie.classify.Label;
import edu.arizona.biosemantics.micropie.extract.regex.AbstractCharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.AntibioticSensitivityExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellDiameterExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellLengthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellShapeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellSizeExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CellWidthExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.CharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.regex.FermentationProductsExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.FermentationSubstratesNotUsed;
import edu.arizona.biosemantics.micropie.extract.regex.GcExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthNaclOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthPhOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempMaxExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempMinExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.GrowthTempOptimumExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.ICharacterValueExtractorProvider;
import edu.arizona.biosemantics.micropie.extract.regex.InorganicSubstancesNotUsedExtractor;
import edu.arizona.biosemantics.micropie.extract.regex.OrganicCompoundsNotUsedOrNotHydrolyzedExtractor;
import edu.arizona.biosemantics.micropie.io.CSVAbbreviationReader;
import edu.arizona.biosemantics.micropie.io.CSVSentenceReader;
import edu.arizona.biosemantics.micropie.io.CharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.io.ICharacterValueExtractorReader;
import edu.arizona.biosemantics.micropie.io.ISentenceReader;
import edu.arizona.biosemantics.micropie.model.MultiClassifiedSentence;
import edu.arizona.biosemantics.micropie.model.Sentence;
import edu.arizona.biosemantics.micropie.model.SentenceMetadata;
import edu.arizona.biosemantics.micropie.model.TaxonTextFile;
import edu.arizona.biosemantics.micropie.transform.ITextNormalizer;
import edu.arizona.biosemantics.micropie.transform.TextNormalizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

public class Config extends AbstractModule {


	
	// private String characterListString = "16S rRNA accession #|Family|Genus|Species|Strain|Genome size|%G+C|Other genetic characteristics|Cell shape|Pigments|Cell wall|Motility|Biofilm formation|Habitat isolated from|Oxygen Use|Salinity preference|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|NaCl minimum|NaCl optimum|NaCl maximum|Host|Symbiotic|Pathogenic|Disease Caused|Metabolism (energy & carbon source)|Carbohydrates (mono & disaccharides)|Polysaccharides|Amino Acids|Alcohols|Fatty Acids|Other Energy or Carbon Sources|Fermentation Products|Polyalkanoates (plastics)|Other Metabolic Product|Antibiotic Sensitivity|Antibiotic Resistant|Cell Diameter|Cell Long|Cell Wide|Cell Membrane & Cell Wall Components|External features|Filterability|Internal features|Lysis Susceptibility|Physiological requirements|Antibiotics|Secreted Products|Storage Products|Tests|Pathogen Target Organ|Complex Mixtures|Inorganic|Metals|Nitrogen Compounds|Organic|Organic Acids|Other";
	
	// Verison 2, March 08, 2015 Sunday
	private String characterListString = "%G+C|Cell shape|Cell diameter|Cell length|Cell width|Cell relationships&aggregations|Gram stain type|Cell membrane & cell wall components|External features|Internal features|Motility|Pigment compounds|Biofilm formation|Filterability|Lysis susceptibility|Habitat isolated from|NaCl minimum|NaCl optimum|NaCl maximum|pH minimum|pH optimum|pH maximum|Temperature minimum|Temperature optimum|Temperature maximum|Pressure preference |Aerophilicity|Magnesium requirement for growth|Vitamins and Cofactors required for growth|Antibiotic sensitivity|Antibiotic resistant|Antibiotic production|Colony shape |Colony margin|Colony texture|Colony color |Film test result|Spot test result|Fermentation Products|Antibiotic production|Methanogenesis products|Other Metabolic Product|Tests positive|Tests negative|Symbiotic relationship|Host|Pathogenic|Disease caused|Pathogen target Organ|Haemolytic&haemadsorption properties|organic compounds used or hydrolyzed|organic compounds not used or not hydrolyzed|inorganic substances used|inorganic substances not used|fermentation substrates used|fermentation substrates not used|Other genetic characteristics|Other physiological characteristics";

	
	private String celsius_degreeReplaceSourcePattern = "(" +
			"\\s?˚C\\s?|" +
			"\\s?˚ C\\s?|" +
			"\\s?\"C\\s?|" +
			"\\s?\" C\\s?|" +
			"\\s?◦C\\s?|" +
			"\\s?◦ C\\s?|" +
			"\\s?°C\\s?|" +
			"\\s?° C\\s?|" +
			"\\s?\\”C\\s?|" +
			"\\s?\\” C\\s?|" +
			"\\s?u C\\s?" +
			")";
	
	

	/** INPUT DATA **/
	// private String trainingFile = "split-training-base-140310.csv";
	
	// private String trainingFile = "training_data/split-training-base-140603.csv";
	// split-training-base-150110.csv
	private String trainingFile = "training_data/split-training-base-150110.csv";
	
	
	private String svmLabelAndCategoryMappingFile = "svmlabelandcategorymapping_data/SVMLabelAndCategoryMapping.txt";
	
	private String testFolder = "input";
	private String characterValueExtractorsFolder = "CharacterValueExtractors";
	private String abbreviationFile = "abbrevlist/abbrevlist.csv";
	private String resFolder = "res";
	private String kbFolder = "kb";
	private String dataHolderFolder = "dataholder";

	// private String uspBaseString = "usp_small_test";
	// private String uspBaseString = "usp_base_new";
	private String uspBaseString = "usp_base";
	private String uspFolder = "usp_base/dep/0";
	
	// => don't useFolder anymore !!
	
	private String uspBaseZipFileName = "usp_base.zip";
	
	private int nGramMinSize = 1;
	private int nGramMaxSize = 1;
	private int nGramMinFrequency = 1;
	private String nGramTokenizerOptions = "-delimiters ' ' -max 1 -min 1";
	private String stringToWordVectorOptions = "-W " + Integer.MAX_VALUE + " -T -L -M 1 -tokenizer weka.core.tokenizer.NGramTokenizer " + nGramTokenizerOptions + "";
	private String multiFilterOptions = "-D -F weka.filters.unsupervised.attribute.StringToWordVector " + stringToWordVectorOptions + "";
	private String libSVMOptions = "-S 0 -D 3 -K 2 -G 0 -R 0 -N 0.5 -M 100 -C 2048 -P 1e-3";
	
	/** OUTPUT DATA **/
	private String predicitonsFile = "predictions.csv";
	private String matrixFile = "matrix.csv";
	private String uspString= "usp";
	private String uspResultsDirectory = "usp_results";
	
	/** PROCESSING **/
	private boolean parallelProcessing = false;
	private int maxThreads = 1;
		
	@Override
	protected void configure() {
		bind(IRun.class).to(TrainTestRun.class).in(Singleton.class);
		
		bind(new TypeLiteral<LinkedHashSet<String>>() {}).annotatedWith(Names.named("Characters"))
			.toProvider(new Provider<LinkedHashSet<String>>() {
				@Override
				public LinkedHashSet<String> get() {
					return new LinkedHashSet<String>(Arrays.asList(characterListString.split("\\|")));
				}
		}).in(Singleton.class);

		bind(String.class).annotatedWith(Names.named("resFolder")).toInstance(resFolder);
		bind(String.class).annotatedWith(Names.named("kbFolder")).toInstance(kbFolder);
		bind(String.class).annotatedWith(Names.named("dataHolderFolder")).toInstance(dataHolderFolder);
		
		bind(String.class).annotatedWith(Names.named("celsius_degreeReplaceSourcePattern")).toInstance(
				celsius_degreeReplaceSourcePattern);
		
		bind(String.class).annotatedWith(Names.named("uspBaseString")).toInstance(
				uspBaseString);
		
		bind(String.class).annotatedWith(Names.named("uspBaseZipFileName")).toInstance(
				uspBaseZipFileName);

		
		
		bind(String.class).annotatedWith(Names.named("uspString")).toInstance(
				uspString);
		
		bind(String.class).annotatedWith(Names.named("uspResultsDirectory")).toInstance(uspResultsDirectory);
		
		bind(String.class).annotatedWith(Names.named("trainingFile")).toInstance(
				trainingFile);
		
		bind(String.class).annotatedWith(Names.named("svmLabelAndCategoryMappingFile")).toInstance(
				svmLabelAndCategoryMappingFile);
		
		bind(String.class).annotatedWith(Names.named("testFolder")).toInstance(
				testFolder);
		
		bind(String.class).annotatedWith(Names.named("uspFolder")).toInstance(
				uspFolder);
		
		bind(String.class).annotatedWith(Names.named("characterValueExtractorsFolder")).toInstance(
				characterValueExtractorsFolder);
		
		bind(String.class).annotatedWith(Names.named("abbreviationFile")).toInstance(
				abbreviationFile);
		
		bind(String.class).annotatedWith(Names.named("predictionsFile")).toInstance(
				predicitonsFile);
		
		bind(String.class).annotatedWith(Names.named("matrixFile")).toInstance(
				matrixFile);
		
		bind(Integer.class).annotatedWith(Names.named("FilterDecorator_NGramMinSize"))
				.toInstance(nGramMinSize);
		
		bind(Integer.class).annotatedWith(Names.named("FilterDecorator_NGramMaxSize"))
				.toInstance(nGramMaxSize);
		
		bind(Integer.class).annotatedWith(Names.named("FilterDecorator_MinFrequency"))
				.toInstance(nGramMinFrequency);
		
		bind(Boolean.class).annotatedWith(Names.named("parallelProcessing")).toInstance(
				parallelProcessing);
		
		bind(Integer.class).annotatedWith(Names.named("maxThreads")).toInstance(
				maxThreads);
		
		bind(String.class).annotatedWith(Names.named("MultiFilterOptions")).toInstance(multiFilterOptions);
		
		bind(String.class).annotatedWith(Names.named("LibSVMOptions")).toInstance(libSVMOptions);
				
		bind(new TypeLiteral<Set<ICharacterValueExtractor>>() {}).toInstance(getCharacterValueExtractors(characterValueExtractorsFolder, 
		 		uspResultsDirectory, uspString));
		
		bind(ISentenceReader.class).to(CSVSentenceReader.class).in(Singleton.class);
		
		bind(ITextNormalizer.class).to(TextNormalizer.class);
		
		bind(new TypeLiteral<List<ILabel>>() {}).annotatedWith(Names.named("MultiSVMClassifier_Labels"))
			.toProvider(new Provider<List<ILabel>>() {
				@Override
				public List<ILabel> get() {
					Label[] labels = Label.values();
					List<ILabel> result = new ArrayList<ILabel>(labels.length);
					for(Label label : labels)
						result.add(label);
					return result;
				}
		});
		
		bind(StanfordCoreNLP.class).annotatedWith(Names.named("TokenizeSSplit")).toProvider(new Provider<StanfordCoreNLP>() {
			@Override
			public StanfordCoreNLP get() {
				Properties stanfordCoreProperties = new Properties();
				stanfordCoreProperties.put("annotators", "tokenize, ssplit");
				return new StanfordCoreNLP(stanfordCoreProperties);
			}
		}).in(Singleton.class);
		
		bind(StanfordCoreNLP.class).annotatedWith(Names.named("TokenizeSSplitPosParse")).toProvider(new Provider<StanfordCoreNLP>() {
			@Override
			public StanfordCoreNLP get() {
				Properties stanfordCoreProperties = new Properties();
				stanfordCoreProperties.put("annotators", "tokenize, ssplit, pos, parse");
				return new StanfordCoreNLP(stanfordCoreProperties);
			}
		});
		
		bind(LexicalizedParser.class).toProvider(new Provider<LexicalizedParser>() {
			@Override
			public LexicalizedParser get() {
				return LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<TokenizerFactory<CoreLabel>>() {}).toProvider(new Provider<TokenizerFactory<CoreLabel>>() {
			@Override
			public TokenizerFactory<CoreLabel> get() {
				return PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<LinkedHashMap<String, String>>() {}).annotatedWith(Names.named("Abbreviations"))
			.toProvider(new Provider<LinkedHashMap<String, String>>() {
			@Override
			public LinkedHashMap<String, String> get() {
				CSVAbbreviationReader abbreviationReader = new CSVAbbreviationReader();
				try {
					abbreviationReader.setInputStream(new FileInputStream(abbreviationFile));
					return abbreviationReader.read();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return new LinkedHashMap<String, String>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<Sentence, MultiClassifiedSentence>>() {})
			.annotatedWith(Names.named("SentenceClassificationMap")).toProvider(new Provider<Map<Sentence, MultiClassifiedSentence>>() {
			@Override
			public Map<Sentence, MultiClassifiedSentence> get() {
				return new HashMap<Sentence, MultiClassifiedSentence>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<Sentence, SentenceMetadata>>() {})
			.annotatedWith(Names.named("SentenceMetadataMap")).toProvider(new Provider<Map<Sentence, SentenceMetadata>>() {
			@Override
			public Map<Sentence, SentenceMetadata> get() {
				return new HashMap<Sentence, SentenceMetadata>();
			}
		}).in(Singleton.class);
		
		bind(new TypeLiteral<Map<TaxonTextFile, List<Sentence>>>() {})
			.annotatedWith(Names.named("TaxonSentencesMap")).toProvider(new Provider<Map<TaxonTextFile, List<Sentence>>>() {
			@Override
			public Map<TaxonTextFile, List<Sentence>> get() {
				return new HashMap<TaxonTextFile, List<Sentence>>();
			}
		}).in(Singleton.class);
		
		bind(ICharacterValueExtractorProvider.class).to(CharacterValueExtractorProvider.class).in(Singleton.class);
		
		weka.core.logging.Logger.log(weka.core.logging.Logger.Level.INFO, "Weka Logging started"); 
	}

	private Set<ICharacterValueExtractor> getCharacterValueExtractors(String extratorsDirectory, String uspResultsDirectory, 
			String uspString) {
		Set<ICharacterValueExtractor> extractors = new HashSet<ICharacterValueExtractor>();
		
		File inputDir = new File(extratorsDirectory);
		if(!inputDir.exists() || inputDir.isFile()) 
			return extractors;
		
		ICharacterValueExtractorReader extractorReader = new CharacterValueExtractorReader(
				uspResultsDirectory, uspString);
		for(File file : inputDir.listFiles()) {
			try {
				ICharacterValueExtractor extractor = extractorReader.read(file);
				extractors.add(extractor);
			} catch(Exception e) {
				log(LogLevel.ERROR, "Could not load extractor in file: " + file.getAbsolutePath() + "\nIt will be skipped", e);
			}
		}
		
		//Add additional more "customized" extractors than the universal keyword based one
		//e.g.
		// extractors.add(new CellSizeExtractor(Label.c1));
		//extractors.add(new CellSizeExtractor(Label.c2));
		
		extractors.add(new GcExtractor(Label.c1));
		extractors.add(new CellDiameterExtractor(Label.c3));
		extractors.add(new CellLengthExtractor(Label.c4));
		extractors.add(new CellWidthExtractor(Label.c5));

		extractors.add(new GrowthNaclMinExtractor(Label.c17));
		extractors.add(new GrowthNaclOptimumExtractor(Label.c18));
		extractors.add(new GrowthNaclMaxExtractor(Label.c19));
		
		extractors.add(new GrowthPhMinExtractor(Label.c20));
		extractors.add(new GrowthPhOptimumExtractor(Label.c21));
		extractors.add(new GrowthPhMaxExtractor(Label.c22));		
		
		
		extractors.add(new GrowthTempMinExtractor(Label.c23));
		extractors.add(new GrowthTempOptimumExtractor(Label.c24));
		extractors.add(new GrowthTempMaxExtractor(Label.c25));
		
		extractors.add(new OrganicCompoundsNotUsedOrNotHydrolyzedExtractor(Label.c52));
		extractors.add(new InorganicSubstancesNotUsedExtractor(Label.c54));
		extractors.add(new FermentationSubstratesNotUsed(Label.c56));

		// extractors.add(new FermentationProductsExtractor(Label.c6));
		// extractors.add(new AntibioticSensitivityExtractor(Label.c4));
		
		
		return extractors;
	}

	public void setInputDirectory(String inputDirectory) {
		testFolder = inputDirectory + File.separator + "input";
		
		// trainingFile = inputDirectory + File.separator + "training_data" + File.separator + "split-training-base-140603.csv";
		// 150123-Training-Sentences.csv
		// trainingFile = inputDirectory + File.separator + "training_data" + File.separator + "150123-Training-Sentences.csv";
		// 150130-Training-Sentences-new.csv
		trainingFile = inputDirectory + File.separator + "training_data" + File.separator + "150130-Training-Sentences-new.csv";

		
		svmLabelAndCategoryMappingFile = inputDirectory + File.separator + "svmlabelandcategorymapping_data" + File.separator + "SVMLabelAndCategoryMapping.txt";
		
		
		
		characterValueExtractorsFolder = inputDirectory + File.separator + "CharacterValueExtractors";
		abbreviationFile = inputDirectory + File.separator + "abbrevlist/abbrevlist.csv";
		resFolder = inputDirectory + File.separator + "res";
		kbFolder = inputDirectory + File.separator + "kb";
		dataHolderFolder = inputDirectory + File.separator + "dataholder";
		uspBaseString = inputDirectory + File.separator + "usp_base";
		uspBaseZipFileName = inputDirectory + File.separator + "usp_base.zip";
		
		
		uspFolder = inputDirectory + File.separator + "usp_base/dep/0";
	}
	
	public void setOutputDirectory(String outputDirectory) {
		predicitonsFile = outputDirectory + File.separator + "predictions.csv";
		matrixFile = outputDirectory + File.separator + "matrix.csv";
		uspString= outputDirectory + File.separator + "usp";
		new File(uspString).mkdirs();
		uspResultsDirectory = outputDirectory + File.separator + "usp_results";
		new File(uspResultsDirectory).mkdirs();
	}

}
