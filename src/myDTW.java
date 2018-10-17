import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.FLOAD;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;

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
	
	public List<String> extractFilesInFolder(String folderAbsolutePath) throws IOException{
		Path p, temp;
		DirectoryStream<Path> stream;
		Iterator<Path> iterator;
		String fileRelativePath, folderRelativePath;
		
		// Extraire les fichiers dans le dossier folderAbsolutePath
		p = Paths.get(folderAbsolutePath);
		stream = Files.newDirectoryStream(p);
		List<String> testFiles = new ArrayList<>(); 
		try {
			iterator = stream.iterator();
			while(iterator.hasNext()) {
				temp = iterator.next();
				fileRelativePath = temp.toString().substring(System.getProperty("user.dir").length(),temp.toString().length());
				testFiles.add(fileRelativePath);
			}
		}
		finally {
			stream.close();
		}
		testFiles.sort(null);
		/*System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Les fichiers dans "+folderAbsolutePath);
		for(int i = 0; i < testFiles.size();i++) {
			System.out.println(i+" "+testFiles.get(i));
		}
		System.out.println("--------------------------------------------------------------------------------------");*/
		return testFiles;
	}
	
	/**Cette fonction implémente la matrice de confusion entre les fichiers sons test dans le dossier testPath, 
	 * avec les fichiers sons de reference dans le dossier referencePath
	 * Elle retourne la somme de la diagonale de cette matrice / le nombre total d'ordres differents 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * */
	public float matriceDeConfusion(String testAbsolutePath, String referenceAbsolutePath, int nbrLocuteur) throws IOException, InterruptedException{
		int nbrOrdre, trace, indexMin;
		int[][] matriceConf;
		float distanceMin, d;
		
		int mfccTestLength, mfccRefLength;
		MFCC[] mfccTest, mfccRef;
		Field fieldTest, fieldRef;
		
		// recuperer la liste des fichiers dans testAbsolutePath
		List<String> testFiles = this.extractFilesInFolder(testAbsolutePath);
		// recuperer la liste des fichiers dans referenceAbsolutePath
		List<String> refFiles = this.extractFilesInFolder(referenceAbsolutePath);
		System.out.println("--------------------------------------------------------------------------------------");
		for(int i = 0; i < testFiles.size();i++) {
			System.out.println(i+" "+testFiles.get(i));
		}
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("--------------------------------------------------------------------------------------");
		for(int i = 0; i < refFiles.size();i++) {
			System.out.println(i+" "+refFiles.get(i));
		}
		System.out.println("--------------------------------------------------------------------------------------");
		nbrOrdre = testFiles.size();
		System.out.println("Il y a "+nbrOrdre+" ordres.");

		matriceConf = new int[nbrOrdre][nbrOrdre];
		
		// Appel a l'extracteur par defaut (calcul des MFCC)
	    Extractor extractor = Extractor.getExtractor();
	    WindowMaker windowMaker = new MultipleFileWindowMaker(testFiles);
	    WindowMaker windowMaker2 = new MultipleFileWindowMaker(refFiles);
	    System.out.println("nblocuteur = "+nbrLocuteur);
		for(int i = 0; i < nbrOrdre; i++) {// pour chaque fichier dans test, comparer avec les fichiers dans ref
			// Recuperer le MFCC  du fichier dans test
			mfccTestLength = myDTWtest2.FieldLength(testFiles.get(i));
			mfccTest = new MFCC[mfccTestLength];
			for (int k = 0; k < mfccTestLength; k++) {
				mfccTest[k] = extractor.nextMFCC(windowMaker);
			}
			// Construire le field de mfccTest
			fieldTest = new Field(mfccTest);
			for (int l = 0; l < nbrLocuteur; l++) {
				// Pour chaque locuteur tester avec test
				indexMin = 0;
				distanceMin = Float.MAX_VALUE;
				for (int j = 0 ; j < nbrOrdre; j++) {
					int index = j*nbrLocuteur + l;
					System.out.println("index = "+index);
					// Recuperer le MFCC  du fichier dans ref
					mfccRefLength = myDTWtest2.FieldLength(refFiles.get(j));
					mfccRef = new MFCC[mfccRefLength];
					for (int k = 0; k < mfccRefLength; k++) {
						mfccRef[k] = extractor.nextMFCC(windowMaker2);
					}
					// Construire le field de mfccRef
					fieldRef = new Field(mfccRef);
					// Calculer la distance entre fieldTest et fieldRef 
					d = DTWDistance(fieldTest, fieldRef);
					int temporar = j+l;
					System.out.println("i = "+j);
					//System.out.println("myDTW - valeur distance "+testFiles.get(i).toString()+"-"+
					//refFiles.get(i)+"   "+d);
					if (d < distanceMin) {
						distanceMin = d;
						indexMin = j;
					}
				}
				matriceConf[indexMin][i]++;
			}
		}
		
		// print matrice de confusion
		for(int i = 0; i < nbrOrdre; i++) {
			for (int j = 0; j < nbrOrdre; j++) {
				System.out.print(" "+matriceConf[i][j]+" ");
			}
			System.out.println();
		}
		
		trace = 0;
		for (int i = 0; i < nbrOrdre; i++) {
			trace += matriceConf[i][i];
		}
		return trace/(nbrOrdre);
		
	}

}
