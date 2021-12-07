package jplag.clustering;

import jplag.*;
import jplag.options.Options;
import jplag.options.util.Messages;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class calculates, based on the similarity matrix, the hierarchical
 * clustering of the documents, using MIN, MAX and AVR methods.
 */
public class Clusters {

	private static final Logger LOGGER = Logger.getLogger(Clusters.class.getName());

	private ArrayList<Submission> submissions;

	public ArrayList<Submission> getSubmissions() {
		return  submissions;
	}

	private final HashSet<Submission> neededSubmissions = new HashSet<>();

	public HashSet<Submission> getNeededSubmissions() {
		return neededSubmissions;
	}

	private float maxMergeValue = 0;

	public float getMaxMergeValue() {
		return maxMergeValue;
	}

	private final Program program;
	private final Messages msg;
	
	public Clusters(Program program){
		this.program=program;
		this.msg=program.getMsg();
	}

	public Cluster calculateClustering(ArrayList<Submission> submissions) {
		this.submissions = submissions;
		Cluster clustersCalculateClustering = null;
		
		switch (this.program.getClusterType()) {
			case Options.MIN_CLUSTER:
			case Options.MAX_CLUSTER:
			case Options.AVR_CLUSTER:
				clustersCalculateClustering = minMaxAvrClustering();
				break;
			default:
		}
		
		return clustersCalculateClustering;
	}

	public String getType() {
		switch (this.program.getClusterType()) {
			case Options.MIN_CLUSTER:
				return msg.getString("Clusters.MIN_single_link");
			case Options.MAX_CLUSTER:
				return msg.getString("Clusters.MAX_complete_link");
			case Options.AVR_CLUSTER:
				return msg.getString("Clusters.AVR_group_average");
			default:
				return msg.getString("Clusters.unknown");
		}
	}

	/** Min clustering... */
	public Cluster minMaxAvrClustering() {
		int nrOfSubmissions = submissions.size();
		boolean minClustering = (Options.MIN_CLUSTER == this.program.getClusterType());
		boolean maxClustering = (Options.MAX_CLUSTER == this.program.getClusterType());
        SimilarityMatrix simMatrix = this.program.getSimilarity();
		
		ArrayList<Cluster> clustersminMax = new ArrayList<>(submissions.size());
		for (int i=0; i<nrOfSubmissions; i++)
			clustersminMax.add(new Cluster(i,this));
		
		while (clustersminMax.size() > 1) {
			int indexA=-1;
			int indexB=-1;
			float maxSim = -1;
			int nrOfClusters = clustersminMax.size();
			
			// find similarity
			for (int a=0; a<(nrOfClusters-1); a++) {
				Cluster cluster = clustersminMax.get(a);
				for (int b=a+1; b<nrOfClusters; b++) {
					float sim;
					if (minClustering)
						sim = cluster.maxSimilarity(clustersminMax.get(b), simMatrix);
					else if (maxClustering)
						sim = cluster.minSimilarity(clustersminMax.get(b), simMatrix);
					else
						sim = cluster.avrSimilarity(clustersminMax.get(b), simMatrix);
					if (sim > maxSim) {
						maxSim = sim;
						indexA = a;
						indexB = b;
					}
				}
			}
			
			if (maxSim > maxMergeValue)
				maxMergeValue = maxSim;
			
			// now merge these clusters
			Cluster clusterA = clustersminMax.get(indexA);
			Cluster clusterB = clustersminMax.get(indexB);
			clustersminMax.remove(clusterA);
			clustersminMax.remove(clusterB);
			clustersminMax.add(new Cluster(clusterA, clusterB, maxSim,this));
		}
		return clustersminMax.get(0);
	}

	private ArrayList<Cluster> getClusters(Cluster clustering, float threshold) {
		ArrayList<Cluster> clustersGetClusters = new ArrayList<>();
		
		// First determine the clusters
		Deque<Cluster> deque = new Deque<>() {
			@Override
			public void addFirst(Cluster cluster) {

			}

			@Override
			public void addLast(Cluster cluster) {

			}

			@Override
			public boolean offerFirst(Cluster cluster) {
				return false;
			}

			@Override
			public boolean offerLast(Cluster cluster) {
				return false;
			}

			@Override
			public Cluster removeFirst() {
				return null;
			}

			@Override
			public Cluster removeLast() {
				return null;
			}

			@Override
			public Cluster pollFirst() {
				return null;
			}

			@Override
			public Cluster pollLast() {
				return null;
			}

			@Override
			public Cluster getFirst() {
				return null;
			}

			@Override
			public Cluster getLast() {
				return null;
			}

			@Override
			public Cluster peekFirst() {
				return null;
			}

			@Override
			public Cluster peekLast() {
				return null;
			}

			@Override
			public boolean removeFirstOccurrence(Object o) {
				return false;
			}

			@Override
			public boolean removeLastOccurrence(Object o) {
				return false;
			}

			@Override
			public boolean add(Cluster cluster) {
				return false;
			}

			@Override
			public boolean offer(Cluster cluster) {
				return false;
			}

			@Override
			public Cluster remove() {
				return null;
			}

			@Override
			public Cluster poll() {
				return null;
			}

			@Override
			public Cluster element() {
				return null;
			}

			@Override
			public Cluster peek() {
				return null;
			}

			@Override
			public boolean addAll(Collection<? extends Cluster> c) {
				return false;
			}

			@Override
			public void push(Cluster cluster) {

			}

			@Override
			public Cluster pop() {
				return null;
			}

			@Override
			public boolean remove(Object o) {
				return false;
			}

			@Override
			public boolean contains(Object o) {
				return false;
			}

			@Override
			public int size() {
				return 0;
			}

			@Override
			public Iterator<Cluster> iterator() {
				return null;
			}

			@Override
			public Iterator<Cluster> descendingIterator() {
				return null;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public Object[] toArray() {
				return new Object[0];
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return null;
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				return false;
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				return false;
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				return false;
			}

			@Override
			public void clear() {

			}
		};
		deque.push(clustering);
		while (!deque.isEmpty()) {
			Cluster current = deque.pop();
			
			if (current != null && current.size() == 1) {
				clustersGetClusters.add(current);  // singleton clusters
			} else {
				if (current != null && current.getSimilarity() >= threshold) {
					clustersGetClusters.add(current);
				} else if (current != null){
					deque.push(current.getLeft());
					deque.push(current.getRight());
				}
			}
		}
		return clustersGetClusters;
	}
  
	/** Print it! */
	public  String printClusters(Cluster clustering, float threshold,
			HTMLFile f) {
		int maxSize = 0;
		
		ArrayList<Cluster> clustersPrintClusters = getClusters(clustering, threshold);

		for (Cluster cluster : clustersPrintClusters) {
			if (cluster.size() > maxSize)
				maxSize = cluster.size();
		}
		
		TreeSet<Cluster> sorted = new TreeSet<>(clustersPrintClusters);

		// Now print them:
		return outputClustering(f, sorted, maxSize);
	}

	/** This method returns the distribution HTML codes as a string */
	private String outputClustering(HTMLFile f, Collection<Cluster> allClusters,
			int maxSize) {
		int[] distribution = new int[maxSize+1];
		int max = 0;
		for (int i=0; i<=maxSize; i++)
			distribution[i] = 0;
		
		// Now output the clustering:
		f.println("<TABLE CELLPADDING=2 CELLSPACING=2>");
		
		f.println("<TR><TH ALIGN=center BGCOLOR=#8080ff>"
			+ msg.getString("Clusters.Cluster_number")
			+ "<TH ALIGN=center BGCOLOR=#8080ff>"
			+ msg.getString("Clusters.Size")
			+ "<TH ALIGN=center BGCOLOR=#8080ff>"
			+ msg.getString("Clusters.Threshold")
			+ "<TH ALIGN=center BGCOLOR=#8080ff>"
			+ msg.getString("Clusters.Cluster_members")
			+ "<TH ALIGN=center BGCOLOR=#8080ff>"
			+ msg.getString("Clusters.Most_frequent_words") + "</TR>");
		Iterator<Cluster> clusterI = allClusters.iterator();
		for (int i=1; clusterI.hasNext(); i++) {
			Cluster cluster = clusterI.next();
			if (max < ++distribution[cluster.size()])
				max = distribution[cluster.size()];
			
			// no singleton clusters
			if (cluster.size() == 1)
				continue;
			
			f.print("<TR><TD ALIGN=center BGCOLOR=#8080ff>" + i
				+ "<TD ALIGN=center BGCOLOR=#c0c0ff>" + cluster.size()
				+ "<TD ALIGN=center BGCOLOR=#c0c0ff>" + cluster.getSimilarity()
				+ "<TD ALIGN=left BGCOLOR=#c0c0ff>");
			
			// sort names
			TreeSet<Submission> sortedSubmissions = new TreeSet<>();
			for (int x=0; x<cluster.size(); x++) {
				sortedSubmissions.add(
					submissions.get(cluster.getSubmissionAt(x)));
			}
			
			for (Iterator<Submission> iter=sortedSubmissions.iterator(); iter.hasNext();) {
				Submission sub = iter.next();
				int index = submissions.indexOf(sub);
				f.print("<A HREF=\"submission"+index+".html\">"+sub.getName()+"</A>");
				if (iter.hasNext())
					f.print(", ");
				neededSubmissions.add(sub); // write files for these.
			}
			
			if (this.program.getLanguage() instanceof jplag.text.Language) {
				f.println("<TD ALIGN=left BGCOLOR=#c0c0ff>" +
					ThemeGenerator.generateThemes(sortedSubmissions,
						this.program.getThemewords(),
						true,this.program));
			} else {
				f.println("<TD ALIGN=left BGCOLOR=#c0c0ff>-");
			}
			
			f.println("</TR>");
		}
		f.println("</TABLE>\n<P>\n");
		
		f.println("<H5>" + msg.getString("Clusters.Distribution_of_cluster_size")
			+ ":</H5>");
		
		StringBuilder text;
		text = new StringBuilder("<TABLE CELLPADDING=1 CELLSPACING=1>\n");
		text.append("<TR><TH ALIGN=center BGCOLOR=#8080ff>").append(msg.getString("Clusters.Cluster_size")).append("<TH ALIGN=center BGCOLOR=#8080ff>").append(msg.getString("Clusters.Number_of_clusters")).append("<TH ALIGN=center BGCOLOR=#8080ff>.</TR>\n");
		for (int i=0; i<=maxSize; i++) {
			if (distribution[i] == 0) continue;
			text.append("<TR><TD ALIGN=center BGCOLOR=#c0c0ff>").append(i).append("<TD ALIGN=right BGCOLOR=#c0c0ff>").append(distribution[i]).append("<TD BGCOLOR=#c0c0ff>\n");
			int barLength = 70;
			text.append("#".repeat(Math.max(0, distribution[i] * barLength / max)));
			if (distribution[i] * barLength / max == 0) {
				if (distribution[i]==0)
					text.append(".");
				else
					text.append("#");
			}
			text.append("</TR>\n");
		}
		text.append("</TABLE>\n");
		
		f.print(text);
		return text.toString();
	}

	/* Dendrograms... */
	public int makeDendrograms(File root, Cluster clustering)
				throws jplag.ExitException {
		HTMLFile f = this.program.getReport().openHTMLFile(root, "dendro.html");
		f.println("<!DOCTYPE HTML PUBLIC \"-//DTD HTML 3.2//EN\">");
		f.println("<HTML>\n<HEAD>\n<TITLE>"
			+ msg.getString("Clusters.Dendrogram") + "</TITLE>\n"
			+ "<script language=\"JavaScript\" type=\"text/javascript\" "
			+ "src=\"fields.js\">\n</script>\n</HEAD>\n<BODY>");
		f.println("<H1>" + msg.getString("Clusters.Dendrogram") + "</H1>");
		
		f.println("<form name=\"data\" action=\"\">");
		f.println("<table border=\"0\">");
		f.println("<tr><td>" + msg.getString("Clusters.Cluster_size") + ":</td>"
			+ "<td><input type=\"text\" readonly name=\"size\" size=\"5\"></td>");
		f.println("<td rowspan=\"3\">" + msg.getString("Clusters.Themewords")
			+ ":</td><td rowspan=\"3\"><textarea cols=\"80\" rows=\"3\" readonly "
			+ "name=\"theme\"></textarea></td></tr>");
		f.println("<tr><td>" + msg.getString("Clusters.Threshold")
			+ ":</td><td><input type=\"text\" readonly name=\"thresh\" "
			+ "size=\"6\"></td></tr>");
		f.println("<tr><td>" + msg.getString("Clusters.Documents")
			+ ":</td><td><input type=\"text\" readonly name=\"docs\" "
			+ "size=\"30\"></td></tr>");
		f.println("</table>\n</form>");
		
		f.println(paintDendrogram(new File(root, "dendro.gif"), clustering));
		f.println("<P><IMG SRC=\"dendro.gif\" ALT=\""
			+ msg.getString("Clusters.Dendrogram_picture")
			+ "\" USEMAP=\"#Dendrogram\"></P>");
		f.println("</BODY>\n</HTML>");
		f.close();
		return f.bytesWritten();
	}
	
	private String trimStringToLength(String text, int l) {
		int trim = l;
		if (trim > text.length())
			trim = text.length();
		return text.substring(0, trim);
	}
	
	private void paintCoords(int xSize, int ySize) {
		float yStep = 1;
		while ((yStep*(ySize-50)/(threshold-lowThreshold)) < 20)
			yStep += 1;
		
		FontMetrics metrics = g.getFontMetrics();
		int height = metrics.getAscent();
		for (float y=lowThreshold; y<threshold; y+=yStep) {
			int yCoord = 10 + (int)((y-lowThreshold)*(ySize-50)/(threshold-lowThreshold));
			
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(45, yCoord, xSize, yCoord);
			g.setColor(Color.BLACK);
			
			String text = "" + y;
			text = trimStringToLength(text, 5);
			int width = metrics.stringWidth(text);
			g.drawString(text, 40-width, yCoord+height/2);
		}
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(45, ySize-40, xSize, ySize-40);
		g.setColor(Color.BLACK);
		
		String text = "" + threshold;
		text = trimStringToLength(text, 5);
		int width = metrics.stringWidth(text);
		g.drawString(text, 40-width, ySize-40+height/2);
		
		g.drawLine(45, 10, 45, ySize-35);
		g.drawLine(45, ySize-35, xSize, ySize-35);
	}
	
	private static final int MAX_VERT_LINES = 200;
	public String paintDendrogram(File f, Cluster clustering) {

		lowThreshold = 0;
		threshold = (int)maxMergeValue + 1;
		
		do {
			threshold = threshold - (float) 1;
			clustersArrayList = getClusters(clustering, threshold);
		} while (clustersArrayList.size() > MAX_VERT_LINES);
		
		int size = clustersArrayList.size();
		factor = 1000 / size;

		int xSize = factor*size + 50;
		int ySize = 500 + 50;
		BufferedImage image = new BufferedImage(xSize+1, ySize+1,
			BufferedImage.TYPE_BYTE_INDEXED);
		g = (Graphics2D)image.getGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 30+xSize, 30+ySize);
		g.setPaintMode();
		
		paintCoords(xSize, ySize);
		
		mapString = "<map name=\"Dendrogram\">\n";
		minX = 50;
		minY = 10;
		maxY = ySize-40;
		drawCluster(clustering);
		g.setColor(Color.GRAY);
		g.drawLine(clustering.x, clustering.y, clustering.x, minY);
		
		try (FileOutputStream fo = new FileOutputStream(f)) {
			GIFEncoder encode = new GIFEncoder(image);
			encode.write(fo);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Exception occur", e);
		}
		return mapString+"</map>";
	}
	
	private  int minX;
	private int minY;
	private int maxY;
	private  int factor;
	private  ArrayList<Cluster> clustersArrayList;
	private  float threshold;
	private float lowThreshold;
	private  Graphics2D g;
	private  String mapString;
	
	public void drawCluster(Cluster cluster) {
		int index = clustersArrayList.indexOf(cluster);
		if (index != -1) {
			cluster.y = maxY;
			cluster.x = minX + index * factor;
			if (cluster.size() > 1) g.setColor(Color.RED);
			else g.setColor(Color.BLACK);
			g.drawRect(cluster.x-1, cluster.y-cluster.size(), 2, 1+cluster.size());
		} else {
			Cluster left = cluster.getLeft();
			Cluster right = cluster.getRight();
			drawCluster(left);
			drawCluster(right);
			int yBar = minY + (int)((maxY-minY)*(cluster.getSimilarity()/threshold));
			g.setColor(Color.DARK_GRAY);
			if (left.y>yBar) {
				g.drawLine(left.x, left.y-1, left.x, yBar);
				writeMap(left, yBar);
			}
			if (right.y>yBar) {
				g.drawLine(right.x, right.y-1, right.x, yBar);
				writeMap(right, yBar);
			}
			g.setColor(Color.BLACK);
			g.drawLine(left.x, yBar, right.x, yBar);
			cluster.x = (right.x+left.x) / 2;
			cluster.y = yBar;
		}
	}
	
	public void writeMap(Cluster cluster, float yBar) {
		HashSet<Submission> subSet = new HashSet<>(cluster.size());
		StringBuilder documents = new StringBuilder();
		for (int i=0; i<cluster.size(); i++) {
			Submission sub = submissions.get(cluster.getSubmissionAt(i));
			documents.append(sub.getName()).append(" ");
			subSet.add(sub);
		}
		documents = new StringBuilder(documents.toString().trim());
		String theme = ThemeGenerator.generateThemes(subSet,
			this.program.getThemewords(),false,this.program);
		mapString += "<area shape=\"rect\" coords=\"" + (cluster.x-2) + ","
			+ (yBar) + "," + (cluster.x+2) + "," + (cluster.y+2)
			+ "\" onMouseover=\"set('" + cluster.size() + "','"
			+ trimStringToLength(String.valueOf(cluster.getSimilarity()),6)
			+ "','" + trimStringToLength(documents.toString(), 50) + "','" + theme + "')\" ";
		mapString += "nohref>\n";
	}
}
