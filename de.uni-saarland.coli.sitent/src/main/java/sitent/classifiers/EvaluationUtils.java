package sitent.classifiers;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class EvaluationUtils {

	static Logger log = Logger.getLogger(EvaluationUtils.class.getName());

	/**
	 * Given a confusion matrix represented as a nested map, outputs a confusion
	 * matrix, accuracy and per-class statistics for precision, recall and
	 * F-measure, as well as macro-averages.
	 * 
	 * @param confMatrix
	 * @param w
	 * @param values
	 */
	public static void printResults(Map<String, Map<String, Integer>> confMatrix, PrintWriter w, String[] values) {

		List<String> classes = new LinkedList<String>();
		for (String v : values) {
			classes.add(v);
		}
		Collections.sort(classes);
		Object[][] table = new String[classes.size() + 1][];
		for (int i = 0; i < classes.size() + 1; i++) {
			table[i] = new String[classes.size() + 1];
		}
		table[0][0] = "";
		String outputPattern = "%15s";

		// header with predictions
		for (int i = 0; i < classes.size(); i++) {
			table[0][i + 1] = classes.get(i).substring(0, Math.min(classes.get(i).length(), 12));
			outputPattern += "%15s";

		}

		// rows with counts
		for (int i = 0; i < classes.size(); i++) {
			table[i + 1][0] = classes.get(i).substring(0, Math.min(classes.get(i).length(), 12));
			for (int j = 0; j < classes.size(); j++) {
				if (!confMatrix.containsKey(classes.get(i))
						|| !confMatrix.get(classes.get(i)).containsKey(classes.get(j))) {
					table[i + 1][j + 1] = "0";
				} else {
					table[i + 1][j + 1] = confMatrix.get(classes.get(i)).get(classes.get(j)).toString();
				}
			}
		}

		for (Object[] row : table) {
			log.info(String.format(outputPattern + "\n", row));
			if (w != null) {
				w.println(String.format(outputPattern, row));
			}
		}

		// output P, R, F per class
		Map<String, Double> precision = new HashMap<String, Double>();
		Map<String, Double> recall = new HashMap<String, Double>();
		Map<String, Double> fMeasure = new HashMap<String, Double>();
		double macroPrecision = 0;
		double macroRecall = 0;
		double macroFMeasure = 0;
		double accuracy = 0;
		double total = 0;
		
		for (String val : confMatrix.keySet()) {
			// skip classes that were never predicted

			Map<String, Integer> predictions = confMatrix.get(val);
			int goldSum = 0;
			for (int count : predictions.values()) {
				goldSum += count;
			}
			total += goldSum;
			
			double r = 0;
			if (confMatrix.containsKey(val) && confMatrix.get(val).containsKey(val)) {
				r = confMatrix.get(val).get(val) / (double) goldSum;
			}
			recall.put(val, r);
			int predictedSum = 0;
			for (String val2 : confMatrix.keySet()) {
				if (confMatrix.get(val2).containsKey(val)) {
					predictedSum += confMatrix.get(val2).get(val);
				}
			}
			double p = 0;
			if (confMatrix.containsKey(val) && confMatrix.get(val).containsKey(val)) {
				accuracy += confMatrix.get(val).get(val);
				p = confMatrix.get(val).get(val) / (double) predictedSum;
			}
			precision.put(val, p);
			double f = 2 * p * r / (p + r);
			if (p == 0 && r == 0) {
				f = 0;
			}
			fMeasure.put(val, f);
			if (f > 0)
				macroFMeasure += f;
			if (p > 0)
				macroPrecision += p;
			if (r > 0)
				macroRecall += r;
		}
		macroFMeasure /= (double) confMatrix.keySet().size();
		macroRecall /= (double) confMatrix.keySet().size();
		macroPrecision /= (double) confMatrix.keySet().size();
		double fOfMacro = 2 * macroPrecision * macroRecall / (macroPrecision + macroRecall);

		log.info("\nClass details");
		w.println("\nClass details:");
		w.println(String.format("%15s%15s%15s%15s", new Object[] { "", "precision", "recall", "f1-measure" }));
		for (String val : confMatrix.keySet()) {
			String line = String.format("%15s%15s%15s%15s",
					new Object[] { val, String.format("%.2f", precision.get(val) * 100),
							String.format("%.2f", recall.get(val) * 100),
							String.format("%.2f", fMeasure.get(val) * 100) });
			log.info(line);
			if (w != null) {
				w.println(line);
			}
		}
		String line = String.format("%15s%15s%15s%15s",
				new Object[] { "macro", String.format("%.2f", macroPrecision * 100),
						String.format("%.2f", macroRecall * 100), String.format("%.2f", macroFMeasure * 100) });
		if (w != null) {
			w.println(line);
			w.println("F-of-macro: " + String.format("%.2f", fOfMacro * 100));
		}

		log.info(line);
		log.info("F-of-macro: " + String.format("%.2f", fOfMacro * 100));
		
		log.info("");
		accuracy /= total;
		log.info("observed agreement / accuracy: " + String.format("%.2f", accuracy * 100));
		w.println("observed agreement / accuracy: " + String.format("%.2f", accuracy * 100));
		

	}

}
