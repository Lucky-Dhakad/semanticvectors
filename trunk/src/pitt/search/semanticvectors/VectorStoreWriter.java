/**
   Copyright (c) 2007, University of Pittsburgh

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:

   * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided
   with the distribution.

   * Neither the name of the University of Pittsburgh nor the names
   of its contributors may be used to endorse or promote products
   derived from this software without specific prior written
   permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
**/

package pitt.search.semanticvectors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.lang.Float;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;

/**
 * This class provides methods for serializing a VectorStore to disk.<p>
 * The serialization currently presumes that the object (in the ObjectVectors)
 * should be serialized as a String. <p>
 * The implementation uses Lucene's I/O package, which proved much faster
 * than the native java.io.DataOutputStream. <p>
 * In the current implementation, VectorStoreWriter objects have no
 * internal fields, since vecLength is now a global variable. The
 * writing methods could therefore be made static and done without
 * instantiation; we've left the current (slightly awkward looking)
 * instance method approach for now to see if the current
 * implementation of vecLength and writers holds up in practice.
 * @see ObjectVector
 */
public class VectorStoreWriter {

  /**
   * Empty constructor method to give you a notional "instance" from which to call
   * class methods.
   */
  public VectorStoreWriter() {}

  /**
   * @param vectorFileName The name of the file to write to
   * @param objectVectors The vector store to be written to disk
   */
  public boolean WriteVectors(String vectorFileName, VectorStore objectVectors) {
    try {
			File vectorFile = new File(vectorFileName);
			String parentPath = vectorFile.getParent();
			if (parentPath == null) parentPath = "";
			FSDirectory fsDirectory = FSDirectory.getDirectory(parentPath);
      IndexOutput outputStream = fsDirectory.createOutput(vectorFile.getName());

      Enumeration<ObjectVector> vecEnum = objectVectors.getAllVectors();
      float[] tmpVector = new float[ObjectVector.vecLength];

      int counter = 0;
      System.err.println("About to write vectors to file " + vectorFile);

      /* Write header giving number of dimensions for all vectors. */
      outputStream.writeString("-dimensions");
      outputStream.writeInt(ObjectVector.vecLength);

      /* Write each vector. */
      while (vecEnum.hasMoreElements()) {
        if ((counter % 10000 == 0) || (counter < 10000 && counter % 1000 == 0)) {
          System.err.print(counter + " ... ");
        }
        ++counter;
        ObjectVector objectVector = vecEnum.nextElement();
        outputStream.writeString(objectVector.getObject().toString());
        tmpVector = objectVector.getVector();
        for (int i = 0; i < ObjectVector.vecLength; ++i) {
          outputStream.writeInt(Float.floatToIntBits(tmpVector[i]));
        }
      }
      System.err.println("Finished writing vectors.");
      outputStream.close();
			fsDirectory.close();
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Outputs a vector store as a plain text file.
   * @param vectorTextFile The name of the file to write to
   * @param objectVectors The vector store to be written to disk
   */
  public boolean WriteVectorsAsText(String vectorTextFile, VectorStore objectVectors) {
    try{
      BufferedWriter outBuf = new BufferedWriter(new FileWriter(vectorTextFile));
      Enumeration<ObjectVector> vecEnum = objectVectors.getAllVectors();

      int counter = 0;
      System.err.println("About to write vectors to text file: " + vectorTextFile);

      /* Write header giving number of dimensions for all vectors. */
      outBuf.write("-dimensions|" + ObjectVector.vecLength + "\n");

      /* Write each vector. */
      while (vecEnum.hasMoreElements()) {
        if ((counter % 10000 == 0) || (counter < 10000 && counter % 1000 == 0)) {
          System.err.print(counter + " ... ");
        }
        ++counter;
        ObjectVector objectVector = vecEnum.nextElement();
        outBuf.write(objectVector.getObject().toString() + "|");
        float[] tmpVector = objectVector.getVector();
        for (int i = 0; i < ObjectVector.vecLength; ++i) {
          outBuf.write(Float.toString(tmpVector[i]));
          if (i != ObjectVector.vecLength - 1) {
            outBuf.write("|");
          }
        }
        outBuf.write("\n");
      }
      outBuf.close();
      System.err.println("Finished writing vectors.");
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}