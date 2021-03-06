
import javax.crypto.*;
import java.security.*;
import javax.crypto.spec.*;

import java.util.Random;

import java.io.PrintStream;

// this file contains simply cracks an encryption key using a brute force method
// it is implemented in parallel in java

class BruteForceDES implements Runnable
{
	private long start;
	private long end;
	private SealedObject sldObj;
	private SealedDES sldDES;

	public void run() {
		long runstart = System.currentTimeMillis();
		// SealedDES deccipher = new SealedDES ();

		SealedDES deccipher = new SealedDES ();

		// Search for the right key
		for ( long i = this.start; i < this.end; i++ )
		{
			// Set the key and decipher the object
			deccipher.setKey ( i );
			String decryptstr = deccipher.decrypt (this.sldObj);
			
			// Does the object contain the known plaintext
			if (( decryptstr != null ) && ( decryptstr.indexOf ( "Hopkins" ) != -1 ))
			{
				//  Remote printlns if running for time.
				System.out.println (  "Found decrypt key " + i + " producing message: " + decryptstr );
			}
			
			// Update progress every once in awhile.
			//  Remote printlns if running for time.
			if ( i % 100000 == 0 )
			{ 
				long elapsed = System.currentTimeMillis() - runstart;
				System.out.println ( "Searched key number " + i + " at " + elapsed + " milliseconds.");
			}
		}

	}

	public BruteForceDES () 
	{
		// this.sldDES = new SealedDES();
	}
		
	// Constructor: initialize the cipher
	public BruteForceDES (long _start, long _end, SealedObject _sldObj) 
	{
		this.start = _start;
		this.end = _end;
		this.sldObj = _sldObj;
	}

	// Program demonstrating how to create a random key and then search for the key value.
	public static void main ( String[] args )
	{
		if ( 2 != args.length )
		{
			System.out.println ("Usage: java BruteForceDES number_threads key_size_in_bits");
			return;
		}
		
		// create object to printf to the console
		PrintStream p = new PrintStream(System.out);

		// Get the argument
		long keybits = Long.parseLong ( args[1] );

		long maxkey = ~(0L);
		maxkey = maxkey >>> (64 - keybits);
		
		// Create a simple cipher
		SealedDES enccipher = new SealedDES ();
		
		// Get a number between 0 and 2^64 - 1
		Random generator = new Random ();
		long key =  generator.nextLong();
		
		// Mask off the high bits so we get a short key
		key = key & maxkey;
		
		// Set up a key
		enccipher.setKey ( key ); 
		
		// Generate a sample string
		String plainstr = "Johns Hopkins afraid of the big bad wolf?";
		
		// Encrypt
		SealedObject sldObj = enccipher.encrypt ( plainstr );
		
		// Here ends the set-up.  Pretending like we know nothing except sldObj,
		// discover what key was used to encrypt the message.
		
		// Get and store the current time -- for timing
		long runstart;
		runstart = System.currentTimeMillis();
		
		int numthreads = Integer.parseInt ( args[0] );
		Thread[] threads = new Thread[numthreads];
		
		long range = (maxkey + 1) / numthreads;

		for (int i = 0; i < numthreads; i++) {
			long s = i * range;
			long e = (i+1) * range;
			threads[i] = new Thread(new BruteForceDES(s, e, sldObj));
			threads[i].start();
		}

		for (int i = 0; i < numthreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted.  Exception: " + e.toString() +
					" Message: " + e.getMessage()) ;
				return;
			}
		}

		// Output search time
		long elapsed = System.currentTimeMillis() - runstart;
		long keys = maxkey + 1;
		System.out.println ( "Completed search of " + keys + " keys at " + elapsed + " milliseconds.");
	}
}
