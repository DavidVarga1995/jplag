package jplag;


/*
 * Diese Klasse implementiert den GSTiling Algorithmus
 * Allerdings ist sie sehr spezifisch auf die Klassen "Structure", "Token",
 * sowie "Matches" und "Match" ausgelegt
 */
public class GSTiling implements TokenConstants {
	private final Matches matches = new Matches();
	private final Program program;

	public GSTiling(Program program){
		this.program=program;
	}

	/*
	 * creating hashes.
	 * The hash-code will be written in every Token for the next <hash_length> token.
	 * (includes the Token itself)
	 * Das Ganze laeuft in linearer Zeit.
	 * condition: 1 < hashLength < 26   !!!
	 */

	public final void create_hashes(Structure s, int hashLength, boolean makeTable) {
		// Hier wird die obere Grenze der calculateHash-Laenge festgelegt.
		// Sie ist bestimmt durch die Bitzahl des 'int' Datentyps und der Anzahl
		// der Token.
		if (hashLength<1) hashLength = 1;
		hashLength = (hashLength<26 ? hashLength : 25);

		if (s.size()<hashLength) return;

		int modulo = ((1 << 6) - 1);   // Modulo 64!

		int loops = s.size()-hashLength;
		s.table = (makeTable ? new Table(3*loops) : null);
		int hash = 0;
		int i;
		int hashedLength = 0;
		for (i=0; i<hashLength; i++) {
			hash = (2*hash) + (s.getTokens()[i].type & modulo);
			hashedLength++;
			if (s.getTokens()[i].marked)
				hashedLength = 0;
		}
		int factor = (hashLength != 1 ? (2<<(hashLength-2)) : 1);

		if (makeTable) {
			for (i=0; i<loops; i++) {
				if (hashedLength >= hashLength) {
					s.getTokens()[i].hash = hash;
					s.table.add(hash, i);   // add into hashtable
				} else
					s.getTokens()[i].hash = -1;
				hash -= factor * (s.getTokens()[i].type & modulo);
				hash = (2*hash) + (s.getTokens()[i+hashLength].type & modulo);
				if (s.getTokens()[i+hashLength].marked)
					hashedLength = 0;
				else
					hashedLength++;
			}
		} else {
			for (i=0; i<loops; i++) {
				s.getTokens()[i].hash = (hashedLength >= hashLength) ? hash : -1;
				hash -= factor * (s.getTokens()[i].type & modulo);
				hash = (2*hash) + (s.getTokens()[i+hashLength].type & modulo);
				if (s.getTokens()[i+hashLength].marked)
					hashedLength = 0;
				else
					hashedLength++;
			}
		}
		s.hashLength = hashLength;
	}

	public final AllMatches compare(Submission subA, Submission subB) {
		Submission A, B, tmp;
		A = subB;
		B = subA;
		// if hashtable exists in first but not in second structure: flip around!
		if (B.getStruct().table == null && A.getStruct().table != null) {
			tmp = A;
			A = B;
			B = tmp;
		}

		return compare(A, B, this.program.getMinTokenMatch());
	}

	// first parameter should contain the smaller sequence!!! 
	private AllMatches compare(Submission subA, Submission subB, int mml) {
		Structure structA = subA.getStruct();
		Structure structB = subB.getStruct();

		// FILE_END used as pivot

		// init
		Token[] A = structA.getTokens();
		Token[] B = structB.getTokens();
		int lengthA = structA.size()-1;  // minus pivots!
		int lengthB = structB.size()-1;  // minus pivots!
		AllMatches allMatches = new AllMatches(subA,subB);

		if (lengthA < mml || lengthB < mml)
			return allMatches;

		// Initialize
		if(!program.useBasecode()) {
			for(int i = 0; i <= lengthA; i++)
				A[i].marked = A[i].type == FILE_END || A[i].type == SEPARATOR_TOKEN;

			for(int i = 0; i <= lengthB; i++)
				B[i].marked = B[i].type == FILE_END || B[i].type == SEPARATOR_TOKEN;
		} else {
			for(int i = 0; i <= lengthA; i++)
				A[i].marked = A[i].type == FILE_END || A[i].type == SEPARATOR_TOKEN || A[i].basecode;

			for(int i = 0; i <= lengthB; i++)
				B[i].marked = B[i].type == FILE_END || B[i].type == SEPARATOR_TOKEN || B[i].basecode;
		}

		// start:
		if (structA.hashLength != this.program.getMinTokenMatch()) {
			create_hashes(structA, mml, false);
		}
		if (structB.hashLength != this.program.getMinTokenMatch()
				|| structB.table == null) {
			create_hashes(structB, mml, true);
		}

		int maxmatch;
		int[] elemsB;

		do {
			maxmatch = mml;
			matches.clear();
			for (int x = 0; x <= lengthA - maxmatch; x++) {
				if (A[x].marked || A[x].hash == -1
						|| (elemsB = structB.table.get(A[x].hash)) == null)
					continue;
inner:			for (int i = 1; i <= elemsB[0]; i++) { // elemsB[0] contains the length of the Array
					int y = elemsB[i];
					if (B[y].marked || maxmatch > lengthB - y) continue;

					int j, hx, hy;
					for (j = maxmatch - 1; j >= 0; j--) { //begins comparison from behind
						if (A[hx = x + j].type != B[hy = y + j].type || A[hx].marked || B[hy].marked)
							continue inner;
					}

					// expand match
					j = maxmatch;
					while(A[hx = x + j].type == B[hy = y + j].type && !A[hx].marked && !B[hy].marked)
						j++;

					if (j > maxmatch) {  // new biggest match? -> delete current smaller
						matches.clear();
						maxmatch = j;
					}
					matches.addMatch(x, y, j);  // add match
				}
			}
			for (int i = matches.size() - 1; i >= 0; i--) {
				int x = matches.matches[i].startA;  // begining of sequence A
				int y = matches.matches[i].startB;  // begining of sequence B
				allMatches.addMatch(x, y, matches.matches[i].length);
				//in order that "Match" will be newly build     (because reusing)
				for (int j = matches.matches[i].length; j > 0; j--)
					A[x++].marked = B[y++].marked = true;   // mark all Token!
			}

		} while (maxmatch != mml);

		return allMatches;
	}

	public final AllBasecodeMatches compareWithBasecode(Submission subA, Submission subB) {
		Submission A, B, tmp;
		A = subB;
		B = subA;
		// if hashtable exists in first but not in second structure: flip around!
		if (B.getStruct().table == null && A.getStruct().table != null) {
			tmp = A;
			A = B;
			B = tmp;
		}

		return compareWithBasecode(A, B, this.program.getMinTokenMatch());
	}

	private AllBasecodeMatches compareWithBasecode(Submission subA, Submission subB, int mml) {
		Structure structA = subA.getStruct();
		Structure structB = subB.getStruct();

		// FILE_END used as pivot

		// init
		Token[] A = structA.getTokens();
		Token[] B = structB.getTokens();
		int lengthA = structA.size()-1;  // minus pivots!
		int lengthB = structB.size()-1;  // minus pivots!
		AllBasecodeMatches allBasecodeMatches = new AllBasecodeMatches(subA,subB);

		if (lengthA < mml || lengthB < mml)
			return allBasecodeMatches;

		// Initialize
		for(int i = 0; i <= lengthA; i++)
			A[i].marked = A[i].type == FILE_END || A[i].type == SEPARATOR_TOKEN;

		for(int i = 0; i <= lengthB; i++)
			B[i].marked = B[i].type == FILE_END || B[i].type == SEPARATOR_TOKEN;


		// start:
		if (structA.hashLength != this.program.getMinTokenMatch()) {
			create_hashes(structA, mml, true);
		}
		if (structB.hashLength != this.program.getMinTokenMatch()
				|| structB.table == null) {
			create_hashes(structB, mml, true);
		}

		int maxmatch;
		int[] elemsB;

		do {
			maxmatch = mml;
			matches.clear();
			for (int x = 0; x <= lengthA - maxmatch; x++) {
				if (A[x].marked || A[x].hash == -1 || (elemsB = structB.table.get(A[x].hash)) == null)
					continue;
inner:			for (int i = 1; i <= elemsB[0]; i++) {// elemsB[0] contains the length of the Array
					int y = elemsB[i];
					if (B[y].marked || maxmatch > lengthB - y) continue;

					int j,hx,hy;
					for (j = maxmatch - 1; j >= 0; j--) { // begins comparison from behind
						if (A[hx = x + j].type != B[hy = y + j].type || A[hx].marked || B[hy].marked)
							continue inner;
					}
					// expand match
					j = maxmatch;
					while(A[hx = x + j].type == B[hy = y + j].type && !A[hx].marked && !B[hy].marked)
						j++;

					if (j != maxmatch) {  // new biggest match? -> delete current smaller
						matches.clear();
						maxmatch = j;
					}
					matches.addMatch(x, y, j);  // add match
				}
			}
			for (int i = matches.size() - 1; i >= 0; i--) {
				int x = matches.matches[i].startA;  // beginning in sequence A
				int y = matches.matches[i].startB;  // beginning in sequence B
				allBasecodeMatches.addMatch(x, y, matches.matches[i].length);
				//in order that "Match" will be newly build     (because reusing)
				for (int j = matches.matches[i].length; j > 0; j--){
					A[x].marked = B[y].marked = true;   // mark all Token!
					A[x].basecode = B[y].basecode= true;
					x++; y++;
				}
			}
		} while (maxmatch != mml);

		return allBasecodeMatches;
	}

	public final void resetBaseSubmission(Submission sub){
		Structure tmpStruct = sub.getStruct();
		Token[] tok = tmpStruct.getTokens();
		for (int z = 0; z < tmpStruct.size()-1;z++){
			tok[z].basecode = false;
		}
	}
}
