import com.sun.org.apache.bcel.internal.generic.FLOAD;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Field;

public class myDTW extends DTWHelper {

	@Override
	public float DTWDistance(Field unknown, Field known) {
		// Methode qui calcule le score de la DTW 
		// entre 2 ensembles de MFCC
		int n = unknown.getLength() + 1;
		int m = known.getLength() + 1;
		float left, up, upleft;
		int w0, w1, w2;
		w0 = 1; w1 = 2; w2 = 1;
		float[][] result = new float[n][m]; // matrice d'alignement entre unknown et known
		float d, score;
		
		myMFCCdistance distanceCalculator = new myMFCCdistance();
		
		result[0][0] = 0;
		for (int i = 1; i < m; i++) {
			result[0][i] = Float.MAX_VALUE;
		}
		for (int i = 1 ; i < n ; i++) {
			result[i][0] = Float.MAX_VALUE;
			for (int j = 1 ; j < m ; j++) {
				d = distanceCalculator.distance(unknown.getMFCC(i-1), 
						known.getMFCC(j-1));
				left = result[i][j-1] + w0*d;
				upleft = result[i-1][j-1] + w1*d;
				up = result[i-1][j] + w2*d;
				result[i][j] = Float.min(left, Float.min(upleft, up));
			}
		}
		score = result[n-1][m-1]/(unknown.getLength()+known.getLength());
		return score;
	}

}
