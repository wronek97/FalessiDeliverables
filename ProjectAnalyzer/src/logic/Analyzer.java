package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;

public class Analyzer {
	
	public static String projectPath = "D:\\GitProjects\\BookKeeper";
	public static String projectName = "BookKeeper";
	public static String savePath = projectPath;
	public static String fileExtension = ".java";
	
	public static double discardRate = 0.49;
	
	public static List<Release> releases = null;
	public static List<Ticket> tickets = null;
	public static List<FileMetrics> fileMetrics = null;
	
	public static List<String> releaseCommits = null;
	
	public static Logger logger = Logger.getLogger(Analyzer.class.getName());
	
	public static void main(String[] args) {
		
		int totalVersions = 0;
		int versionsToAnalyze = 0;
		
		try {
			Analyzer.switchVersion("master");
			
			releases = Release.getAllReleases(projectName);
			tickets = Ticket.getFixedBugTickets(projectName, releases);
			Ticket.setProportional(tickets);
			totalVersions = releases.size();
			versionsToAnalyze = (int)Math.floor(totalVersions*(1-discardRate));
			releaseCommits = getReleaseCommits(projectName, projectPath, versionsToAnalyze);
			
			fileMetrics = Analyzer.analyzeProject(fileExtension, versionsToAnalyze);
			
			//Release.printReleases(releases);
			//Ticket.printTickets(tickets);
			//Analyzer.printFiles(fileListsPerVersion);
			
			Release.saveReleasesToCSV(savePath, projectName, releases);
			Ticket.saveTicketsToCSV(savePath, projectName, tickets);
			FileMetrics.saveFileMetricsToCSV(savePath, projectName, projectPath, fileMetrics);
			
			
		} catch (JSONException | IOException e) {
			logger.log(Level.SEVERE, "Error analyzing project", e);
		}
		
	}
	
	public static List<FileMetrics> analyzeProject(String fileExtension, int versionsToAnalyze, boolean discardTests) {
		List<FileMetrics> result = new ArrayList<FileMetrics>();
		
		List<List<String>> filesPerVersion = Analyzer.getFilesName(fileExtension, versionsToAnalyze);
		for(int k = 0; k < versionsToAnalyze; k++) { //calculate metrics of all files in version k
			for(String file : filesPerVersion.get(k)) {
				FileMetrics fm = new FileMetrics(file, k);
				
				result.add(fm);
			}
		}
		
		//calculate all sizes
		for(FileMetrics fm : result) {
			fm.setSize(Analyzer.getFileSize(fm.getName(), fm.getVersion())); //per ora devo aggiungerla a mano perchè ho solo il costruttore con tutte le metriche calcolate
		}
		Analyzer.switchVersion("master");
		
		//calculate all other metrics
		for(FileMetrics fm : result) {
			List<List<Integer>> modifiedLines = Analyzer.getSomeMetric(fm.getName(), fm.getVersion());
			
			Integer sum = 0;
			List<Integer> addedLines = modifiedLines.get(0);
			for(Integer i : addedLines) {
				sum = sum + i;
			}
			fm.setLOC_added(sum);
			if(addedLines.size() != 0) {
				fm.setAVG_LOC_added((double) sum/addedLines.size());
			}
			Integer aux = 0;
			for(Integer j : addedLines) {
				if(j > aux) {
					aux = j;
				}
			}
			fm.setMAX_LOC_added(aux);
			
			Integer diff = 0;
			List<Integer> deletedLines = modifiedLines.get(1);
			for(Integer i : deletedLines) {
				diff = diff + i;
			}
			fm.setLOC_touched(sum+diff);
			
			Integer churnSum = 0;
			List<Integer> churns = modifiedLines.get(2);
			for(Integer i : churns) {
				churnSum = churnSum + i;
			}
			fm.setChurn(churnSum);
			if(churns.size() != 0) {
				fm.setAVG_churn((double) (churnSum)/churns.size());
			}
			aux = 0;
			for(Integer j : churns) {
				if(j > aux) {
					aux = j;
				}
			}
			fm.setMAX_churn(aux);
			
			fm.setNR(modifiedLines.get(3).get(0));
			fm.setNF(modifiedLines.get(4).get(0));
			
			fm.setBugged(Analyzer.isBugged(fm.getName(), fm.getVersion()));
		}
		Analyzer.switchVersion("master");
		
		return result;
	}
	
	private static List<FileMetrics> analyzeProject(String fileExtension, int versionsToAnalyze) {
		return Analyzer.analyzeProject(fileExtension, versionsToAnalyze, true);
	}
	
	private static void switchVersion(String commitID) {
		String[] checkoutCommand = {"cmd.exe", "/c", "git", "checkout", commitID};
		Analyzer.callCMD(projectPath, checkoutCommand);
	}
	
	private static List<List<String>> getFilesName(String fileExtension, int versionsToAnalyze, boolean discardTests){
		List<List<String>> result = new ArrayList<List<String>>();
		
		for(int k=0; k<versionsToAnalyze; k++) {
			//change release of the project folder with a checkout to the release date nearest commit
			Analyzer.switchVersion(releaseCommits.get(k));
			
			List<String> files = null;
			try (Stream<Path> walk = Files.walk(Paths.get(projectPath))) {
				if(discardTests) {
					files = walk.map(x -> x.toString()).filter(f -> f.endsWith(fileExtension)).filter(f -> ( !f.contains("src\\test") && !f.contains("\\tests\\") )).collect(Collectors.toList());
				}
				else {
					files = walk.map(x -> x.toString()).filter(f -> f.endsWith(fileExtension)).collect(Collectors.toList());
				}
				result.add(k, files);
				
		    } catch (IOException e) {
				logger.log(Level.SEVERE, "Error reading folder", e);
		    }
		}
		//switch back to master version
		Analyzer.switchVersion("master");
		
		return result;
	}
	
	private static List<List<String>> getFilesName(String fileExtension, int versionsToAnalyze){
		return Analyzer.getFilesName(fileExtension, versionsToAnalyze, true);
	}
	
	private static int getFileSize(String file, int version) {
		int size = 0;
		
		Analyzer.switchVersion(releaseCommits.get(version));
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
		    while ((line = br.readLine()) != null) {
		       if(!line.isEmpty()) {
					size++;
				}
		    }

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return size;
	}
	
	private static List<List<Integer>> getSomeMetric(String file, int version) { //return a list of 4 list, for addedLines, deletedLines, NR, NF
		List<List<Integer>> modifiedLines = new ArrayList<List<Integer>>();
		
		if(releases != null) {
			List<Integer> insertions = new ArrayList<Integer>();
			List<Integer> deletions = new ArrayList<Integer>();
			List<Integer> churns = new ArrayList<Integer>();
			List<Integer> nr = new ArrayList<Integer>();
			List<Integer> nf = new ArrayList<Integer>();
			Integer r = 0;
			Integer f = 0;
			
			Integer ins = 0;
			Integer del = 0;

			String beforeDate = releases.get(version+1).getReleaseDate().toString().replace("T00:00", "");
			String afterDate = releases.get(version).getReleaseDate().toString().replace("T00:00", "");
			String[] cmd = {"cmd.exe", "/c", "git", "log", "--stat", "--before='" + beforeDate + "'", "--after='" + afterDate + "'", "--", file.replace(projectPath + "\\", "")};
			
			String cmdOutput = Analyzer.callCMD(projectPath, cmd);
			String[] parsedCMD = cmdOutput.split("\n");
			
			String commitTicketID = null;
			
			for(String line : parsedCMD) {
				if(line.startsWith("commit ")) {
					commitTicketID = null;
					ins = 0;
					del = 0;
					r++;
				}
				else {
					if(commitTicketID == null && line.contains(projectName.toUpperCase() + "-")) {
						commitTicketID = projectName.toUpperCase() + "-" + String.valueOf(Analyzer.extractNumber(line));
						if(Analyzer.isFixedTicket(commitTicketID)) {
							f++;
						}
					}
					else if(line.contains("1 file changed,")) {
						String[] parsedLine = line.split(",");
						for(String s : parsedLine) {
							if(s.contains("insertions(+)")){
								ins = Analyzer.extractNumber(s);
								insertions.add(ins);
							} else if(s.contains("deletions(-)")){
								del = Analyzer.extractNumber(s);
								deletions.add(del);
							}
						}
						churns.add(ins-del);
					}
				}
			}
			nr.add(r);
			nf.add(f);
			
			modifiedLines.add(0, insertions);
			modifiedLines.add(1, deletions);
			modifiedLines.add(2, churns);
			modifiedLines.add(3, nr);
			modifiedLines.add(4, nf);
		}
		
		return modifiedLines;
	}
	
	private static boolean isBugged(String file, int version) {
		String firstReleaseDate = releases.get(0).getReleaseDate().toString().replace("T00:00", "");
		String[] cmd = {"cmd.exe", "/c", "git", "log", "--date=iso", "--after='" + firstReleaseDate + "'", "--", file.replace(projectPath + "\\", "")};
		
		String cmdOutput = Analyzer.callCMD(projectPath, cmd);
		String[] parsedCMD = cmdOutput.split("\n");
		
		String commitTicketID;
		for(String line : parsedCMD) {
			if(line.contains(projectName.toUpperCase() + "-")) {
				commitTicketID = projectName.toUpperCase() + "-" + String.valueOf(Analyzer.extractNumber(line));
				Ticket aux = Analyzer.getTicketByName(commitTicketID); // gets ticket by name only if it is in the fixed bug tickets list !!
				if(aux != null) {
					if(aux.getInjectedVersion()-1 <= version && version < aux.getFixedVersion()-1) { //if IV <= version of file < FV, -1 because version in FileMetrics is saved from 0 to n-1 instead of in Tickets (from 1 to n)
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private static boolean isFixedTicket(String ticketID) {
		for(Ticket t : tickets) {
			if(t.getTicketID().equals(ticketID)) {
				return true;
			}
		}
		return false;
	}
	
	private static Ticket getTicketByName(String ticketID) {
		for(Ticket t : tickets) {
			if(t.getTicketID().equals(ticketID)) {
				return t;
			}
		}
		
		return null;
	}
	
	private static String callCMD(String path, String[] commands) {
		ProcessBuilder procBuilder = new ProcessBuilder(commands).directory(new File(path));
		procBuilder.redirectErrorStream(true);
		Process proc = null;
		StringBuilder result = new StringBuilder("");
		
		try {
			proc = procBuilder.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		
			String s = null;
			while ((s = stdInput.readLine()) != null) {
			    result.append(s+"\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}
	
	private static Integer extractNumber(String str) {
        if(str == null || str.isEmpty()){
            return 0;
        }
        
        StringBuilder sb = new StringBuilder("");
        boolean found = false;
        for(char c : str.toCharArray()){
            if(Character.isDigit(c)){
                sb.append(c);
                found = true;
            } else if(found){
                // If we already found a digit before and this char is not a digit, stop looping
                break;                
            }
        }
        
        if(!sb.toString().equals("")){
            return Integer.parseInt(sb.toString());
        }
        
        return 0;
    }
	
	private static List<String> getReleaseCommits(String projectName, String projectPath, int versionsToAnalyze){
		List<Release> releases = null;
		List<String> commits = new ArrayList<String>();
		try {
			releases = Release.getAllReleases(projectName);
		} catch (JSONException | IOException e1) {
			e1.printStackTrace();
		}
		for(int k=0; k<versionsToAnalyze; k++) {
			//find first commit of the release 'K'
			String[] commands = {"cmd.exe", "/c", "git", "log", "--date=iso", "--name-status", "--before='" + releases.get(k).getReleaseDate() + "'", "HEAD"};
			String[] aux = Analyzer.callCMD(projectPath, commands).split("\n", 2);
			commits.add(k, aux[0].replace("commit ", ""));
		}
		
		return commits;
	}
	
	public static void printFiles(List<List<String>> fileList) {
		for(List<String> version : fileList) {
			for(String file : version) {
				System.out.println(fileList.indexOf(version) + " - " + file);
			}
		}
	}
	
}
