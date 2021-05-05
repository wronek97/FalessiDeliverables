package logic;

import weka.core.Instance;
import weka.core.Instances;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import logic.FileMetrics.CSV_Mode;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaTools{
	public static final String CSV = ".csv";
	public static final String ARFF = ".arff";
	
	private WekaTools() {
	    throw new IllegalStateException("WekaTools is a static class");
	}
	
	public static void convertCsvToArff(String csvSource, String arffOutput) {
		CSVLoader loader = new CSVLoader();
		ArffSaver saver = new ArffSaver();
		
		try {
			// load CSV
			loader.setSource(new File(csvSource));
			Instances data = loader.getDataSet();
	
			// save ARFF
			if(!data.isEmpty()) {
				saver.setInstances(data);
				saver.setFile(new File(arffOutput));
				saver.writeBatch();
			}
			else {
				try (FileWriter fileWriter = new FileWriter(arffOutput)) {
					StringBuilder outputBuilder = new StringBuilder("");
					outputBuilder.append("@relation Training1\n\n");
					outputBuilder.append("@attribute Version numeric\n");
					outputBuilder.append("@attribute Size numeric\n");
					outputBuilder.append("@attribute LOC_touched numeric\n");
					outputBuilder.append("@attribute LOC_added numeric\n");
					outputBuilder.append("@attribute MAX_LOC_added numeric\n");
					outputBuilder.append("@attribute AVG_LOC_added numeric\n");
					outputBuilder.append("@attribute Churn numeric\n");
					outputBuilder.append("@attribute MAX_Churn numeric\n");
					outputBuilder.append("@attribute AVG_Churn numeric\n");
					outputBuilder.append("@attribute NR numeric\n");
					outputBuilder.append("@attribute NF numeric\n");
					outputBuilder.append("@attribute Bugged {false,true}\n\n");
					outputBuilder.append("@data\n");
					
					fileWriter.append(outputBuilder.toString());
				}
			}
			
			
		} catch (IOException e) {
			Logger logger = Logger.getLogger(WekaTools.class.getName());
			logger.log(Level.SEVERE, "Error converting csv to arff", e);
		}
		
	}
	
	public static void convertAllCsvToArff(String sourcesPath) {
		List<String> files = null;
		try (Stream<Path> walk = Files.walk(Paths.get(sourcesPath))) {
			files = walk.map(Path::toString).filter(f -> f.endsWith(CSV)).collect(Collectors.toList());
		} catch (IOException e) {
			Logger logger = Logger.getLogger(WekaTools.class.getName());
			logger.log(Level.SEVERE, "Error converting csv to arff", e);
	    }
		
		for(String file : files) {
			WekaTools.convertCsvToArff(file, file.replace(CSV, ARFF));
		}
	}
	
	public static void generateArff(String sourcesPath, String csvSource, String arffOutput) {
		CSVLoader loader = new CSVLoader();
		String fileName = csvSource.replace(sourcesPath, "").replace("\\", "").replace(CSV, "");
		
		try (FileWriter fileWriter = new FileWriter(arffOutput)) {
			StringBuilder outputBuilder = new StringBuilder("");
			outputBuilder.append("@relation " + fileName + "\n\n");
			outputBuilder.append("@attribute Version numeric\n");
			outputBuilder.append("@attribute Size numeric\n");
			outputBuilder.append("@attribute LOC_touched numeric\n");
			outputBuilder.append("@attribute LOC_added numeric\n");
			outputBuilder.append("@attribute MAX_LOC_added numeric\n");
			outputBuilder.append("@attribute AVG_LOC_added numeric\n");
			outputBuilder.append("@attribute Churn numeric\n");
			outputBuilder.append("@attribute MAX_Churn numeric\n");
			outputBuilder.append("@attribute AVG_Churn numeric\n");
			outputBuilder.append("@attribute NR numeric\n");
			outputBuilder.append("@attribute NF numeric\n");
			outputBuilder.append("@attribute Bugged {false,true}\n\n");
			outputBuilder.append("@data\n");
			
			fileWriter.append(outputBuilder.toString());
			
			// load CSV
			loader.setSource(new File(csvSource));
			Instances data = loader.getDataSet();
	
			// save ARFF
			if(!data.isEmpty()) {
				for(Instance ins : data) {
					fileWriter.append(ins.toString() + "\n");
				}
			}
			
		} catch (IOException e) {
			Logger logger = Logger.getLogger(WekaTools.class.getName());
			logger.log(Level.SEVERE, "Error generating arffs", e);
		}
		
	}
	
	public static void generateAllArff(String sourcesPath) {
		List<String> files = null;
		try (Stream<Path> walk = Files.walk(Paths.get(sourcesPath))) {
			files = walk.map(Path::toString).filter(f -> f.endsWith(CSV)).collect(Collectors.toList());
		} catch (IOException e) {
			Logger logger = Logger.getLogger(WekaTools.class.getName());
			logger.log(Level.SEVERE, "Error generating arffs", e);
	    }
		
		for(String file : files) {
			WekaTools.generateArff(sourcesPath, file, file.replace(CSV, ARFF));
		}
	}
	
	public static void walkForward(String sourcesPath, Integer versionsToAnalyze, String savePath, String projectName, CSV_Mode mode) {
		String outname = savePath + "\\" + projectName + "WekaAnalysis.csv";
		String[] classifiersList = {"NaiveBayes", "IBk", "J48"};
		Evaluation[] evaluationsList = new Evaluation[classifiersList.length];
		StringBuilder outputBuilder;
		
		DataSource trainingSource;
		DataSource testSource;
		Instances training;
		Instances testing;
		
		if(mode == CSV_Mode.IT) {
			outputBuilder = new StringBuilder("Version;Classifier;TPR(Bugged);FPR(Bugged);Precision(Bugged);Recall(Bugged);AUC(Bugged);TPR(Not Bugged);FPR(Not Bugged);Precision(Not Bugged);Recall(Not Bugged);AUC(Not Bugged);Kappa;Accuracy\n");
		}
		else {
			outputBuilder = new StringBuilder("Version,Classifier,TPR(Bugged),FPR(Bugged),Precision(Bugged),Recall(Bugged),AUC(Bugged),TPR(Not Bugged),FPR(Not Bugged),Precision(Not Bugged),Recall(Not Bugged),AUC(Not Bugged),Kappa,Accuracy\n");
		}
		
		try (FileWriter fileWriter = new FileWriter(outname)) {
			for(int k = 0; k < versionsToAnalyze; k++) {
				trainingSource = new DataSource(sourcesPath + "\\" + "Training" + (k+1) + ARFF);
				testSource = new DataSource(sourcesPath + "\\" + "Test" + (k+1) + ARFF);
				training = trainingSource.getDataSet();
				testing = testSource.getDataSet();
	
				int numAttr = training.numAttributes();
				training.setClassIndex(numAttr - 1); // l'indice è relativo all'ultima colonna (Bugged)
				testing.setClassIndex(numAttr - 1);
	
				NaiveBayes nbClassifier = new NaiveBayes();
				evaluationsList[0] = new Evaluation(testing);
				nbClassifier.buildClassifier(training);
				evaluationsList[0].evaluateModel(nbClassifier, testing);
				
				IBk ibkClassifier = new IBk(11);
				evaluationsList[1] = new Evaluation(testing);
				ibkClassifier.buildClassifier(training);
				evaluationsList[1].evaluateModel(ibkClassifier, testing);
	
				J48 j48Classifier = new J48();
				evaluationsList[2] = new Evaluation(testing);
				j48Classifier.buildClassifier(training);
				evaluationsList[2].evaluateModel(j48Classifier, testing);
				
				NumberFormat numberFormat;
				String separator;
				if(mode == CSV_Mode.IT) {
					numberFormat = NumberFormat.getInstance(Locale.ITALY);
				    numberFormat.setGroupingUsed(false);
				    separator = ";";
				}
				else {
					numberFormat = NumberFormat.getInstance(Locale.US);
				    numberFormat.setGroupingUsed(false);
				    separator = ",";
				}
				
				for (int i = 0; i < classifiersList.length; i++) {
					outputBuilder.append((k+1) + separator);
					outputBuilder.append(classifiersList[i] + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].truePositiveRate(1)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].falsePositiveRate(1)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].precision(1)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].recall(1)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].areaUnderROC(1)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].truePositiveRate(0)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].falsePositiveRate(0)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].precision(0)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].recall(0)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].areaUnderROC(0)) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].kappa()) + separator);
					outputBuilder.append(numberFormat.format(evaluationsList[i].pctCorrect()/100) + "\n");
				}
			}
			fileWriter.append(outputBuilder.toString());
			
		} catch (Exception e) {
			Logger logger = Logger.getLogger(WekaTools.class.getName());
			logger.log(Level.SEVERE, "Error in Walk-Forward Analysis", e);
		}
	}
	
}
