package logic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

public class FileMetrics {
	private String name;
	private int version;
	private int size;
	private int total_loc_added;
	private int total_loc_touched;
	private int max_loc_added;
	private double avg_loc_added;
	private int churn;
	private int max_churn;
	private double avg_churn;
	private int nR;
	private int nF;
	private boolean bugged;
	
	public FileMetrics(String name, int version, int total_loc_added, int total_loc_touched, int max_loc_added, double avg_loc_added, int churn, int max_churn, double avg_churn, int nR, int nF, boolean bugged) {
		this.name = name;
		this.version = version;
		this.total_loc_added = total_loc_added;
		this.total_loc_touched = total_loc_touched;
		this.max_loc_added = max_loc_added;
		this.avg_loc_added = avg_loc_added;
		this.churn = churn;
		this.max_churn = max_churn;
		this.avg_churn = avg_churn;
		this.nR = nR;
		this.nF = nF;
		this.bugged = bugged;
	}
	
	public FileMetrics(String name, int version) {
		this.name = name;
		this.version = version;
		this.total_loc_added = 0;
		this.total_loc_touched = 0;
		this.max_loc_added = 0;
		this.avg_loc_added = 0;
		this.churn = 0;
		this.max_churn = 0;
		this.avg_churn = 0;
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

	public int getLOC_added() {
		return total_loc_added;
	}

	public void setLOC_added(int total_loc_added) {
		this.total_loc_added = total_loc_added;
	}

	public int getLOC_touched() {
		return total_loc_touched;
	}

	public void setLOC_touched(int total_loc_touched) {
		this.total_loc_touched = total_loc_touched;
	}

	public int getMAX_LOC_added() {
		return max_loc_added;
	}

	public void setMAX_LOC_added(int max_loc_added) {
		this.max_loc_added = max_loc_added;
	}

	public double getAVG_LOC_added() {
		return avg_loc_added;
	}

	public void setAVG_LOC_added(double avg_loc_added) {
		this.avg_loc_added = avg_loc_added;
	}

	public int getChurn() {
		return churn;
	}

	public void setChurn(int churn) {
		this.churn = churn;
	}

	public int getMAX_churn() {
		return max_churn;
	}

	public void setMAX_churn(int max_churn) {
		this.max_churn = max_churn;
	}

	public double getAVG_churn() {
		return avg_churn;
	}

	public void setAVG_churn(double avg_churn) {
		this.avg_churn = avg_churn;
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
	
	
	public static void saveFileMetricsToCSV(String savePath, String projectName, String projectPath, List<FileMetrics> fileMetrics) throws JSONException, IOException {
		String outname = savePath + "\\" + projectName + "Metrics.csv";
		
		try (FileWriter fileWriter = new FileWriter(outname)) {
			StringBuilder outputBuilder = new StringBuilder("Version;FileName;Size;LOC_touched;LOC_added;MAX_LOC_added;AVG_LOC_added;Churn;MAX_Churn;AVG_Churn;NR;NF;Bugged\n");
			
			for (FileMetrics fm : fileMetrics) {
				outputBuilder.append(fm.getVersion()+1 + ";");
				outputBuilder.append(fm.getName().replace(projectPath + "\\", "") + ";");
				outputBuilder.append(fm.getSize() + ";");
				outputBuilder.append(fm.getLOC_touched() + ";");
				outputBuilder.append(fm.getLOC_added() + ";");
				outputBuilder.append(fm.getMAX_LOC_added() + ";");
				outputBuilder.append(String.format("%.2f", fm.getAVG_LOC_added()) + ";");
				outputBuilder.append(fm.getChurn() + ";");
				outputBuilder.append(fm.getMAX_churn() + ";");
				outputBuilder.append(String.format("%.2f", fm.getAVG_churn()) + ";");
				outputBuilder.append(fm.getNR() + ";");
				outputBuilder.append(fm.getNF() + ";");
				outputBuilder.append(fm.isBugged() + "\n");
			}
			fileWriter.append(outputBuilder.toString());
			
		} catch (Exception e) {
			Logger logger = Logger.getLogger(Release.class.getName());
			logger.log(Level.SEVERE, "Error in csv writer", e);
		}
	}
}
