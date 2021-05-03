package logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

public class FileMetrics {
	private String name;
	private int version;
	private int size;
	private int totalLocAdded;
	private int totalLocTouched;
	private int maxLocAdded;
	private double avgLocAdded;
	private int churn;
	private int maxChurn;
	private double avgChurn;
	private int nR;
	private int nF;
	private boolean bugged;
	
	public FileMetrics(String name, int version) {
		this.name = name;
		this.version = version;
		this.totalLocAdded = 0;
		this.totalLocTouched = 0;
		this.maxLocAdded = 0;
		this.avgLocAdded = 0;
		this.churn = 0;
		this.maxChurn = 0;
		this.avgChurn = 0;
		this.nR = 0;
		this.nF = 0;
		this.bugged = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLOCadded() {
		return totalLocAdded;
	}

	public void setLOCadded(int totalLocAdded) {
		this.totalLocAdded = totalLocAdded;
	}

	public int getLOCtouched() {
		return totalLocTouched;
	}

	public void setLOCtouched(int totalLocTouched) {
		this.totalLocTouched = totalLocTouched;
	}

	public int getMAXLOCadded() {
		return maxLocAdded;
	}

	public void setMAXLOCadded(int maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}

	public double getAVGLOCadded() {
		return avgLocAdded;
	}

	public void setAVGLOCadded(double avgLocAdded) {
		this.avgLocAdded = avgLocAdded;
	}

	public int getChurn() {
		return churn;
	}

	public void setChurn(int churn) {
		this.churn = churn;
	}

	public int getMAXchurn() {
		return maxChurn;
	}

	public void setMAXchurn(int maxChurn) {
		this.maxChurn = maxChurn;
	}

	public double getAVGchurn() {
		return avgChurn;
	}

	public void setAVGchurn(double avgChurn) {
		this.avgChurn = avgChurn;
	}

	public int getNR() {
		return nR;
	}

	public void setNR(int nR) {
		this.nR = nR;
	}
	
	public int getNF() {
		return nF;
	}

	public void setNF(int nF) {
		this.nF = nF;
	}

	public boolean isBugged() {
		return bugged;
	}

	public void setBugged(boolean bugged) {
		this.bugged = bugged;
	}
	
	public static List<FileMetrics> getFileMetricsFromName(List<List<String>> filesPerVersion, int versionsToAnalyze){
		List<FileMetrics> result = new ArrayList<>();
		
		for(int k = 0; k < versionsToAnalyze; k++) {
			//calculate metrics of all files in version k
			for(String file : filesPerVersion.get(k)) {
				FileMetrics fm = new FileMetrics(file, k);
				
				result.add(fm);
			}
		}
		
		return result;
	}
	
	public enum CSV_Mode {
		IT("Version;FileName;Size;LOC_touched;LOC_added;MAX_LOC_added;AVG_LOC_added;Churn;MAX_Churn;AVG_Churn;NR;NF;Bugged\n"),
		US("Version,FileName,Size,LOC_touched,LOC_added,MAX_LOC_added,AVG_LOC_added,Churn,MAX_Churn,AVG_Churn,NR,NF,Bugged\n");
		
		private String csvArgs;
		
		CSV_Mode(String args) {
			this.csvArgs = args;
		}
		
		public String getArgs() {
			return this.csvArgs;
		}
		
	}
	
	public static void saveFileMetricsToCSV(String savePath, String projectName, String projectPath, List<FileMetrics> fileMetrics, CSV_Mode mode) throws JSONException, IOException {
		String outname = savePath + "\\" + projectName + "Metrics.csv";
		
		try (FileWriter fileWriter = new FileWriter(outname)) {
			StringBuilder outputBuilder = new StringBuilder(mode.getArgs());
			
			if(mode == CSV_Mode.IT) {
				NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
			    nf.setGroupingUsed(false);
			    
				for (FileMetrics fm : fileMetrics) {
					outputBuilder.append(fm.getVersion()+1 + ";");
					outputBuilder.append(fm.getName().replace(projectPath + "\\", "") + ";");
					outputBuilder.append(fm.getSize() + ";");
					outputBuilder.append(fm.getLOCtouched() + ";");
					outputBuilder.append(fm.getLOCadded() + ";");
					outputBuilder.append(fm.getMAXLOCadded() + ";");
					outputBuilder.append(nf.format(fm.getAVGLOCadded()) + ";");
					outputBuilder.append(fm.getChurn() + ";");
					outputBuilder.append(fm.getMAXchurn() + ";");
					outputBuilder.append(nf.format(fm.getAVGchurn()) + ";");
					outputBuilder.append(fm.getNR() + ";");
					outputBuilder.append(fm.getNF() + ";");
					outputBuilder.append(fm.isBugged() + "\n");
				}
				fileWriter.append(outputBuilder.toString());
			}
			else {
				NumberFormat nf = NumberFormat.getInstance(Locale.US);
			    nf.setGroupingUsed(false);
			    
				for (FileMetrics fm : fileMetrics) {
					outputBuilder.append(fm.getVersion()+1 + ",");
					outputBuilder.append(fm.getName().replace(projectPath + "\\", "") + ",");
					outputBuilder.append(fm.getSize() + ",");
					outputBuilder.append(fm.getLOCtouched() + ",");
					outputBuilder.append(fm.getLOCadded() + ",");
					outputBuilder.append(fm.getMAXLOCadded() + ",");
					outputBuilder.append(nf.format(fm.getAVGLOCadded()) + ",");
					outputBuilder.append(fm.getChurn() + ",");
					outputBuilder.append(fm.getMAXchurn() + ",");
					outputBuilder.append(nf.format(fm.getAVGchurn()) + ",");
					outputBuilder.append(fm.getNR() + ",");
					outputBuilder.append(fm.getNF() + ",");
					outputBuilder.append(fm.isBugged() + "\n");
				}
				fileWriter.append(outputBuilder.toString());
			}
				
			
		} catch (Exception e) {
			Logger logger = Logger.getLogger(Release.class.getName());
			logger.log(Level.SEVERE, "Error in csv writer", e);
		}
	}
	
	public static void saveTrainingsForML(String savePath, List<FileMetrics> fileMetrics, Integer versionsToAnalyze) throws JSONException, IOException {
		File mlPath = new File(savePath + Analyzer.ML_PATH);
		if (!mlPath.exists()){
			mlPath.mkdirs();
		}
		
		for(int k = 0; k < versionsToAnalyze; k++) {
			String trainingFile = savePath + Analyzer.ML_PATH + "\\" + "Training" + (k+1) + ".csv";
			
			try (FileWriter fileWriter = new FileWriter(trainingFile)) {
				StringBuilder outputBuilder = new StringBuilder(CSV_Mode.US.getArgs().replace("FileName,", ""));
				NumberFormat nf = NumberFormat.getInstance(Locale.US);
			    nf.setGroupingUsed(false);
			    
				for (FileMetrics fm : fileMetrics) {
					if(fm.getVersion() < k) {
						outputBuilder.append(fm.getVersion()+1 + ",");
						outputBuilder.append(fm.getSize() + ",");
						outputBuilder.append(fm.getLOCtouched() + ",");
						outputBuilder.append(fm.getLOCadded() + ",");
						outputBuilder.append(fm.getMAXLOCadded() + ",");
						outputBuilder.append(nf.format(fm.getAVGLOCadded()) + ",");
						outputBuilder.append(fm.getChurn() + ",");
						outputBuilder.append(fm.getMAXchurn() + ",");
						outputBuilder.append(nf.format(fm.getAVGchurn()) + ",");
						outputBuilder.append(fm.getNR() + ",");
						outputBuilder.append(fm.getNF() + ",");
						outputBuilder.append(fm.isBugged() + "\n");
					}
				}
				fileWriter.append(outputBuilder.toString());
				
			} catch (Exception e) {
				Logger logger = Logger.getLogger(Release.class.getName());
				logger.log(Level.SEVERE, "Error in training csv writer", e);
			}
			
		}
	}
	
	public static void saveTestsForML(String savePath, List<FileMetrics> fileMetrics, Integer versionsToAnalyze) throws JSONException, IOException {
		File mlPath = new File(savePath + Analyzer.ML_PATH);
		if (!mlPath.exists()){
			mlPath.mkdirs();
		}
		
		for(int k = 0; k < versionsToAnalyze; k++) {
			String testFile = savePath + Analyzer.ML_PATH + "\\" + "Test" + (k+1) + ".csv";
			
			try (FileWriter fileWriter = new FileWriter(testFile)) {
				StringBuilder outputBuilder = new StringBuilder(CSV_Mode.US.getArgs().replace("FileName,", ""));
				NumberFormat nf = NumberFormat.getInstance(Locale.US);
			    nf.setGroupingUsed(false);
			    
				for (FileMetrics fm : fileMetrics) {
					if(fm.getVersion() == k) {
						outputBuilder.append(fm.getVersion()+1 + ",");
						outputBuilder.append(fm.getSize() + ",");
						outputBuilder.append(fm.getLOCtouched() + ",");
						outputBuilder.append(fm.getLOCadded() + ",");
						outputBuilder.append(fm.getMAXLOCadded() + ",");
						outputBuilder.append(nf.format(fm.getAVGLOCadded()) + ",");
						outputBuilder.append(fm.getChurn() + ",");
						outputBuilder.append(fm.getMAXchurn() + ",");
						outputBuilder.append(nf.format(fm.getAVGchurn()) + ",");
						outputBuilder.append(fm.getNR() + ",");
						outputBuilder.append(fm.getNF() + ",");
						outputBuilder.append(fm.isBugged() + "\n");
					}
				}
				fileWriter.append(outputBuilder.toString());
				
			} catch (Exception e) {
				Logger logger = Logger.getLogger(Release.class.getName());
				logger.log(Level.SEVERE, "Error in test csv writer", e);
			}
			
		}
	}
	
}
