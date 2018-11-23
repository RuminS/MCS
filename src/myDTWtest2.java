
import java.io.File;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import fr.enseeiht.danck.voice_analyzer.defaults.DTWHelperDefault;
import Jama.*;
import java.math.*;
public class myDTWtest2 {
		
		static int nbOrdres = 9;
		//protected static final int MFCCLength = 13;
		
		// Fonction permettant de calculer la taille des Fields 
		//c'est-à-dire le nombre de MFCC du Field 
			static int FieldLength(String fileName) throws IOException {
			int counter= 0;
			File file = new File(System.getProperty("user.dir") + fileName);
            for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
            	counter++;
            }
            return 2*Math.floorDiv(counter, 512);
		}
		
		/**Cette fonction permet de tester la reconnaissance de l'archive corpus
		 * On va comparer les résultats donnés par le DTWHHelperDefault et myDWT
		 * On compare chaque enregistrement à tous les enregistrements du corpus
		 * @throws IOException 
		 * @throws InterruptedException 
		 * */
		public static void testCorpus(String chemonAbsolu) throws IOException, InterruptedException {
			int MFCCLength, fileCount;
			DTWHelper myDTWHelper= new myDTW();
			DTWHelper DTWHelperDefault= new DTWHelperDefault();
			String cheminRelatifFichierCsv;
			
			MFCC[] mfcc1, mfcc2;
			Field field1, field2;
			String ordre1, ordre2;
			
			float mydistance;
			float distanceDefault;
			
			// Chemin de recherche des fichiers sons
		    // base = chemonAbsolu : repertoire où il y a les fichiers .csv
			
			// Appel a l'extracteur par defaut (calcul des MFCC)
		    Extractor extractor = Extractor.getExtractor();
		    
			// Load repertoire à tester
			Path folderPath = Paths.get(chemonAbsolu);
			DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath);
			List<String> files = new ArrayList<>(); // liste des chemins dont MultipleFileWindowMaker aura besoin
			try {
				Iterator<Path> iterator = stream.iterator();
				while(iterator.hasNext()) { // pour tous les fichiers dans le repertoire
					Path file = iterator.next();
					cheminRelatifFichierCsv = file.toString().substring(System.getProperty("user.dir").length(),file.toString().length());
					//System.out.println(cheminRelatifFichierCsv);
					files.add(cheminRelatifFichierCsv);
				}
			}
			finally {
				stream.close();
			}
			fileCount = files.size();
			//System.out.println("Il y a "+fileCount+" fichiers en tout.");
		    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		    String s = "/corpus/dronevolant_bruite_csv/";
		    for (int i = 0 ; i < fileCount; i++) {
		    	ordre1 = files.get(i).toString().replaceAll(s, "");
		    	// Recuperer un MFCC mfcc1
		    	MFCCLength= FieldLength(files.get(i));
		    	mfcc1 = new MFCC[MFCCLength];
		    	for (int k = 0; k < mfcc1.length; k++) {
		            mfcc1[k] = extractor.nextMFCC(windowMaker);
		    	}
		    	// construire le field de mfcc1
		    	field1 = new Field(mfcc1);
		        //System.out.println(field1.toString());
		        
		    	System.out.println("Comparaison de "+ordre1+" avec le reste des fichiers.");
		    	System.out.println("-------------------------------------------------------------------------------");
		    	
		    	// Comparer field1 et field1 avec le DTW par defaut et myDTW
		    	mydistance= myDTWHelper.DTWDistance(field1, field1);
		    	distanceDefault= DTWHelperDefault.DTWDistance(field1, field1);
		        System.out.println("["+ordre1+", "+ordre1+"] : myDTW = "+mydistance+", DTWHelperDefault = "+distanceDefault);
		        
		    	// Comparer field1 avec tous les autres fields de tous les autres sons 
		        for (int j = i+1 ; j < fileCount; j++) {
		        	ordre2 = files.get(j).toString().replaceAll(s, "");
		        	// Recuperer mfcc2
		        	MFCCLength= FieldLength(files.get(j));
			    	mfcc2 = new MFCC[MFCCLength];
			    	for (int k = 0; k < mfcc2.length; k++) {
			            mfcc2[k] = extractor.nextMFCC(windowMaker);
			    	}
			    	// construire le field de mfcc2
			    	field2 = new Field(mfcc2);
			        //System.out.println(field2.toString());
			        
			        // Comparer field1 et field2 avec le DTW par defaut et myDTW
			    	mydistance= myDTWHelper.DTWDistance(field1, field2);
			    	distanceDefault= DTWHelperDefault.DTWDistance(field1, field2);
			        
			        System.out.println("["+ordre1+", "+ordre2+"] : myDTW = "+mydistance+", DTWHelperDefault = "+distanceDefault+"      ");
		        }
		        System.out.println();System.out.println();
		    }
		}
		
		/*Fonction de calcul de la moyenne des MFCCs sous la forme d'un vecteur de dimension 13*/
		public static double[] moyenneField (Field field){
			double[] tabRetour=new double[13];
			for (int i=0; i<13; i++){
				tabRetour[i]=0.0;		
			}
			MFCC mfcc;
			double [][] tabTraduit = new double[field.getLength()][13];
			for (int i = 0; i< field.getLength(); i++){
				mfcc = field.getMFCC(i);
				for (int j = 0; j<13; j++)
					tabTraduit[i][j] = mfcc.getCoef(j);
			}
			
			for (int i = 0; i< field.getLength(); i++){
				for (int j = 0; j<13; j++)
					tabRetour[j] = tabRetour[j] + tabTraduit[i][j];
			}
			for (int i=0; i<13; i++){
				tabRetour[i] = tabRetour[i]/field.getLength();
			}
			return tabRetour;
		}
		
		public static double[] moyenne(double[][] matriceX){
			double[] resultat = new double[13];
			for (int i = 0; i< 13; i++)
				resultat[i] = 0;
			for (int j = 0; j < matriceX[0].length; j++){
				for (int i = 0; i < matriceX.length; i++)
					resultat[j] = resultat[j] + matriceX[i][j];
				resultat[j] = resultat[j]/matriceX.length;
			}
			return resultat;
		}
		
		public static double[] retranche(double[] m1, double[] m2){
			
			double[] tabRetour= new double[m1.length];
			for(int i=0 ; i<m1.length; i++){
				tabRetour[i]=m1[i]-m2[i];
			}
			
			return tabRetour;
		}
		
		public static double[][] matriceCentre (String referenceAbsolutePath) throws IOException, InterruptedException{
					
			int mfccRefLength;
			MFCC[] mfccRef;
			double[] gravite;
			myDTW m = new myDTW();
			List<String> refFiles = m.extractFilesInFolder(referenceAbsolutePath);
			double [][] matrice=new double[13][refFiles.size()];
			double[][] tabRetour=new double[refFiles.size()][13];
			Extractor extractor = Extractor.getExtractor();
		    WindowMaker windowMaker = new MultipleFileWindowMaker(refFiles);
			for (int i = 0 ; i<refFiles.size(); i++){
				mfccRefLength = FieldLength(refFiles.get(i));
				mfccRef = new MFCC[mfccRefLength];
				for (int k = 0; k < mfccRefLength; k++) {
					mfccRef[k] = extractor.nextMFCC(windowMaker);
				}
				Field fieldRef = new Field(mfccRef);
				tabRetour[i]=moyenneField(fieldRef);
			}
			gravite=moyenne(tabRetour);
			
			for(int j=0; j<refFiles.size() ; j++){
				tabRetour[j]=retranche(tabRetour[j], gravite);
			}
			
			for(int i=0; i<refFiles.size(); i++){
				for(int j=0; j<13; j++){
					matrice[j][i]=tabRetour[i][j];
				}
			}
			
			return matrice;
		}
		//multiplication de la matrice A et de la B : AxB
		public static double[][] multiplier(double[][] MA, double[][] MB) throws Exception{

			double[][] MC;
			int l,c;
			
			if(MA[0].length != MB.length){
				throw new Exception("La multiplication de deux matrices n'est possible que si le nombre de colonne du premier est égal au nombre de ligne du second!!");
			}
			 
			 /*if(MA.length * MA[0].length < MB.length * MB[0].length){
				l= MB.length;
				c= MB[0].length;
			 }else{
				 l= MA.length;
				 c= MA[0].length;
			 }*/
			l = MA.length;
			c = MB[0].length;
			 
			 MC = new double[l][c];
			 
			 l = 0;
		     for (int i = 0;i < MA.length;i++){ /// Ligne de MA
		    	 c = 0;
	            for (int n = 0;n < MB[0].length;n++){ /// colonne de  MB
	            	
	            	double calcul= 0;
	                for (int m = 0;m < MB.length;m++){  /// colone de MA et ligne de MB
	                    calcul += MA[i][m] * MB[m][n];
	                }
	                MC[l][c] = calcul;
	                c++;
	            }
	            l++;
		     }

			return MC;
		}
		
		public static double[][] transpose(double[][] matrice){
			double[][] matriceRetour=new double[matrice[0].length][matrice.length];
			int n=matrice.length;
			  for (int i = 0; i < n; i++) {
		            for (int j = 0; j < matrice[0].length; j++) {
		                matriceRetour[j][i] = matrice[i][j];
		            }
		      }
			  
			  return matriceRetour;
		}
		
		public static double[][] matriceVarCov(double[][] matrice)throws Exception{
			double[][] matriceRetour;/*=new double[matrice[0].length][matrice[0].length];*/
			
			//System.out.println(matrice.length + " " + matrice[0].length);
			double[][] trans = transpose(matrice);
			//System.out.println(trans.length + " " + trans[0].length);

			/*double[][] mul = {{1,2},{1,2},{1,2}};
			double[][] mul2 = {{2,1},{2,1}};
			matriceRetour = multiplier(mul,mul2);*/
			
			matriceRetour=multiplier(trans, matrice);
			
			//System.out.println(matriceRetour.length + " " + matriceRetour[0].length);
			
			for(int i=0; i<matriceRetour.length; i++){
				for(int j=0; j<matriceRetour.length; j++){
					matriceRetour[i][j]=matriceRetour[i][j]/matrice.length;
				}
			}
			return matriceRetour;
		}
		
		public static double[][] getVectPropres(double[][] matrice){
			  Matrix matrix= new Matrix(matrice);
			  EigenvalueDecomposition e = matrix.eig();
		      Matrix V = e.getV();
		      double[][] tabTemp= V.getArray();
		      double[][] tabTemp2= new double [3][matrice.length];
		      tabTemp2[0]= tabTemp[0];
		      tabTemp2[1]= tabTemp[1];
		      tabTemp2[2] = tabTemp[2];
		      
		   return tabTemp2;
		}
		
		public static double[][] changerBase(double[][] matrice, double[][] nouvelleBase) throws Exception{
			return(multiplier(matrice, nouvelleBase));
		}
		
		public static double[][] ACP(String referenceAbsolutePath) throws Exception{
			double[][] Xc = transpose(matriceCentre(referenceAbsolutePath));
			
			//System.out.println(base.length + " " + base[0].length);
			
			double[][] newBase;
			double[][] matVarCov = matriceVarCov(Xc);
			
			/*for (int i = 0; i < matVarCov.length; i++){
				for (int j = 0; j<matVarCov.length; j++)
					System.out.print((int)matVarCov[i][j]+" ");
				System.out.println();
			}*/
			
			//System.out.println("base: " +base.length + " " + base[0].length);
			
			newBase = getVectPropres(matVarCov);
			
			//System.out.println("newBase: " + newBase.length + " " + newBase[0].length);
			Xc = changerBase(Xc, transpose(newBase));
			return Xc;
		}
		
		public static double distance3D(double[] ref, double[] test){
			double calcul = 0.;
			for (int i=0; i<3; i++)
				calcul = calcul + Math.pow((ref[i]-test[i]),2);
			return Math.sqrt(calcul);
		}
		
		 public static int getKnn(double[][] tabRef,double[] point, int k) {
		        //Determinon les K plus proches voisins:
		        //On trie le tableau pour recupere les k-PP.        
		        Map<Double, Integer> dist = new HashMap<Double,Integer>();        	
		        for (int i = 0; i<tabRef.length; i++)
		        		dist.put(distance3D(tabRef[i], point),i%k);
		        //On trie la map pour pouvoir recupere les k-plus proches voisins
		        Map<Double, Integer> kNN = new TreeMap<Double, Integer>(dist);	        
		        //On determine les occurences des classes:
		        Set keys = kNN.keySet();
		        Iterator it = keys.iterator();
	            int[] value = new int[k];
	            int kFinal = k;
	            for (int i = 0; i<k; i++)
	            	value[i] =0;
		        while (it.hasNext()&&k>0) {
		            Double key = (Double) it.next();
		            value[kNN.get(key)]++;
		            k--;
		        }//end_while	        
		        //En suite ,on determine la classe la plus frequente dans les kNN:
		        int max = 0;
		        int id = 0;
		        for (int j = 0; j<kFinal; j++){
		        	//System.out.println("val j :"+value[j]);
		        	if (value[j] > max){
		        		max = value[j];
		        		id = j;
		        	}
		        	else{
		        		if ( value[j] == max ){
		        			id = j;
		        		}
		        	}
		        		
		        }
		        return id;


		}
		 
		public static int[][] kPlusProches(String referenceAbsolutePath, String testAbsolutePath, int k) throws Exception{
			double[][] tabRef = ACP(referenceAbsolutePath);
			double[][] tabTest = ACP(testAbsolutePath);
			int[][] tabRetour= new int[nbOrdres][nbOrdres];
			int indOrdre = 0; 
			for(int i=0; i<tabTest.length; i++){
				indOrdre = getKnn(tabRef, tabTest[i],k);
				//System.out.println(indOrdre + " ");
				
				tabRetour[indOrdre][i%nbOrdres]++;
			}
			
			return tabRetour;
		}
		
		public static float Taux(int[][] matrice){

			int trace = 0;
			int horsDiag = 0;
			for (int i = 0; i < nbOrdres; i++) {
				trace += matrice[i][i];
				for (int j = 0; j < nbOrdres; j++) {
					if (i!=j) {
						horsDiag += matrice[i][j];
					}
				}
			}
			float tauxReconnaissance = (float) (((float)trace/(float)(trace + horsDiag))*100.);
			//float tauxErreur = (float) (float)horsDiag/(float)(trace + horsDiag)*100;
			//System.out.println("trace = "+trace+" somme totale = "+(trace+horsDiag));
			//System.out.println("taux de reconnaissance = "+tauxReconnaissance+"%");
			//System.out.println("taux d'erreur = "+tauxErreur+"%");
			return tauxReconnaissance;
		}
		
		public static void main(String[] args) throws Exception {
			/*String corpusBruite = "/home/randriamalala/Documents/S1/MODEL_CALCUL_SCIENTIFIQUE/TP1-MCS/corpus/dronevolant_bruite_csv";
			String corpusNonBruite = "/home/randriamalala/Documents/S1/MODEL_CALCUL_SCIENTIFIQUE/TP1-MCS/corpus/dronevolant_bruite_csv";
			System.out.println("------------------------------------------------TEST CORPUS BRUITE ------------------------------------------------");
			testCorpus(corpusBruite);
			System.out.println();System.out.println();
			System.out.println("------------------------------------------------TEST CORPUS NON BRUITE ------------------------------------------------");
			//testCorpus(corpusNonBruite);
			*/
			//testCorpus("/home/randriamalala/Documents/S1/MODEL_CALCUL_SCIENTIFIQUE/TP1-MCS/test_res/audio");
			/*
			int MFCCLength;
			DTWHelper myDTWHelper= new myDTW();
			DTWHelper DTWHelperDefault= new DTWHelperDefault();
			 
			// Chemin de recherche des fichiers sons
		    String base = "/test_res/audio/";
		    
		    // Appel a l'extracteur par defaut (calcul des MFCC)
		    Extractor extractor = Extractor.getExtractor();
		    
			// Etape 1. Lecture de Alpha
		    List<String> files = new ArrayList<>();
		    files.add(base + "Alpha.csv");
		    System.out.println(files.toString());
		    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		    
		    // Etape 2. Recuperation des MFCC du mot Alpha
		    MFCCLength= FieldLength(base+"Alpha.csv");
		    MFCC[] mfccsAlpha = new MFCC[MFCCLength];
		    
	        for (int i = 0; i < mfccsAlpha.length; i++) {
	            mfccsAlpha[i] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) de alpha
	        Field alphaField= new Field(mfccsAlpha);
	        
	        // Affichage du Field 
	        System.out.println(alphaField.toString());
	        // Affichage de chaque MFCC associé à ce field
	        for (int i =0; i<alphaField.getLength(); i++ )
	        	System.out.println(i+": "+alphaField.getMFCC(i).toString());
	        
	        // Si on veut rajouter de nouveaux mots, il suffit de repeter les etapes 1 a 3
	        
	        // Par ex., on peut tester que la distance entre alpha et alpha c'est 0
	        float mydistanceAlphaAlpha= myDTWHelper.DTWDistance(alphaField, alphaField);
	        float distanceAlphaAlphadefault= DTWHelperDefault.DTWDistance(alphaField, alphaField);
	        
	        System.out.println("myDTW - valeur distance Alpha-Alpha calculee : "+mydistanceAlphaAlpha);
	        System.out.println("DTWHelperDefault - valeur distance Alpha-Alpha calculee : "+distanceAlphaAlphadefault);
		
	        // La distance entre Alpha et Bravo
	        
	        // Etape 1. Lecture de Bravo
	        files= new ArrayList<>();
		    files.add(base + "Bravo.csv");
		    MFCCLength= FieldLength(base+"Bravo.csv");
		    windowMaker = new MultipleFileWindowMaker(files);
	
		    // Etape 2. Recuperation des MFCC du mot Bravo
		    MFCC[] mfccsBravo= new MFCC[MFCCLength];
	        for (int i = 0; i < mfccsBravo.length; i++) {
	            mfccsBravo[i] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) de Bravo
	        Field bravoField= new Field(mfccsBravo);
	        
	        float mydistanceAlphaBravo= myDTWHelper.DTWDistance(alphaField, bravoField);
	        float distanceAlphaBravodefault= DTWHelperDefault.DTWDistance(alphaField, bravoField);
	        
	        System.out.println("myDTW - valeur distance Alpha-Bravo calculee : "+mydistanceAlphaBravo);
	        System.out.println("DTWHelperDefault - valeur distance Alpha-Bravo calculee : "+distanceAlphaBravodefault);
	       */
	        myDTW m = new myDTW();
			m.matriceDeConfusion("/home/ubuntu/Master/TP1-MCS/corpus/corpus_enregistrement_perso/Ber",
					"/home/ubuntu/Master/TP1-MCS/corpus/corpus_enregistrement_perso/referenceTim", 5);
			
			
			int[][] tabRet = kPlusProches("/home/ubuntu/Master/TP1-MCS/corpus/corpus_enregistrement_perso/Ber",
					"/home/ubuntu/Master/TP1-MCS/corpus/corpus_enregistrement_perso/referenceTim", 2);
			for (int i=0; i < nbOrdres; i++){
				for (int j=0; j < nbOrdres; j++)
						System.out.print(tabRet[i][j]+" ");
				System.out.println();
			}
			System.out.println("Grâce à la méthode de l'ACP et des k plus proche voisin,"
					+ " nous obtenons un taux de reconnaissance de :" + Taux(tabRet));
			
					
		}
}


