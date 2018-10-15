
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
import java.util.Iterator;
import java.util.List;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import fr.enseeiht.danck.voice_analyzer.defaults.DTWHelperDefault;

public class myDTWtest2 {

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
		
		
		public static void main(String[] args) throws IOException, InterruptedException {
			String corpusBruite = "/home/randriamalala/Documents/S1/MODEL_CALCUL_SCIENTIFIQUE/TP1-MCS/corpus/dronevolant_bruite_csv";
			String corpusNonBruite = "/home/randriamalala/Documents/S1/MODEL_CALCUL_SCIENTIFIQUE/TP1-MCS/corpus/dronevolant_bruite_csv";
			System.out.println("------------------------------------------------TEST CORPUS BRUITE ------------------------------------------------");
			testCorpus(corpusBruite);
			System.out.println();System.out.println();
			System.out.println("------------------------------------------------TEST CORPUS NON BRUITE ------------------------------------------------");
			//testCorpus(corpusNonBruite);
			
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
	        
		}
}


