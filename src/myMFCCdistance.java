import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.MFCCHelper;

public class myMFCCdistance extends MFCCHelper {

	@Override
	public float distance(MFCC mfcc1, MFCC mfcc2) {
		// calcule la distance entre 2 MFCC
		float d = 0;
		float temp;
		int length = mfcc1.getLength(); // =13
		for (int i = 0; i < length; i++) {
			temp = mfcc2.getCoef(i) - mfcc1.getCoef(i);
			d += temp*temp;
		}
		return (float)Math.sqrt(d);
	}

	@Override
	public float norm(MFCC mfcc) {
		// retourne la valeur de mesure de la MFCC (coef d'indice 0 dans la MFCC) 
		// cette mesure permet de determiner s'il s'agit d'un mot ou d'un silence
		return mfcc.getCoef(0);
	}

	@Override
	public MFCC unnoise(MFCC mfcc, MFCC noise) {
		// supprime le bruit de la MFCC passee en parametre
		// soustrait chaque coef du bruit a chaque coef du la MFCC 
		// passee en parametre
		int l = mfcc.getLength();
		float[] tab = new float[l] ;
		float[] signal = new float[l];
		
		float[] signalMfcc = mfcc.getSignal();
		float[] signalNoise = noise.getSignal();
		
		for (int i = 0; i < mfcc.getLength(); i++) {
			tab[i] =  mfcc.getCoef(i) - noise.getCoef(i);
			signal[i] = signalMfcc[i] - signalNoise[i];
		}
		return new MFCC(tab, signal); 
	}

}
