package trigram;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Famous WordCount example
 */
public class WordCount {
  public static void main(String[] args) throws FileNotFoundException, IOException {
  	 final Map<String,List<String>> lemmaMap = new HashMap<String,List<String>>();
  	 /*Create Lemmatizer Map*/

    String csvFile = "new_lemmatizer.csv";
        String line = "";
        String cvsSplitBy = ",";
        // try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
          BufferedReader br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] tokens = line.split(cvsSplitBy);
                String word = tokens[0];
                List<String> lemList = new ArrayList<String>();
                if(tokens.length>1){
                    for(int i = 1;i<tokens.length;i++){
                        lemList.add(tokens[i]);
                        
                    }
                  }
                lemmaMap.put(word, lemList);
                }
              // }

    /*End Create Lemmatizer Map*/

     final StringBuilder fileOutput = new StringBuilder();
     final List<String> outList = new ArrayList<String>();
    SparkConf sparkConf = new SparkConf().setAppName("Word Count").setMaster("local[*]");
    JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);


 
JavaRDD<String> input = sparkContext.textFile(args[0]);

   
    JavaRDD<String> lines = input.filter(new Function<String, Boolean>() {
      @Override
      public Boolean call(String s) throws Exception {
        if(s == null || s.trim().length() < 1) {
          return false;
        }
        return true;
      }
    });

    // Now we have non-empty ss, lets split them into words
    JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
      @Override
      public Iterator<String> call(String s) throws Exception {
          List<String> list = new ArrayList<String>();
          
          String pos = null;
                 Pattern p = Pattern.compile("<([^>]+)>");   // the pattern to search for
//                    Pattern p = Pattern.compile("<.*|.*>");
                    Matcher ml = p.matcher(s);
                    if (ml.find())
    {
      pos = ml.group(1);     
      
    }           
                String pos1 = "<"+pos+">";
//                System.out.println(pos1);
		
                s = s.replaceAll("<([^>]+)>", " ");
		s = s.replaceAll("\t","");
                //s = s.replaceAll("[^a-zA-Z0-9\\\\s+]", " ");
		s = s.replaceAll("[^a-zA-Z\\\\s+]", " ");
                s = s.toLowerCase();
                s = s.replaceAll("j", "i");
                s = s.replaceAll("v","u");
                String[] tokens = s.split("\\s+");
                
                for(int i = 0;i<tokens.length - 2;i++){
          for(int j = i+1;j<tokens.length-1;j++){
            for(int m = i+2;m<tokens.length; m++){
                if(!(tokens[i].length()<2)){
                    if(lemmaMap.containsKey(tokens[i])){
            		List<String> list1 = lemmaMap.get(tokens[i]);
		            for(int l = 0;l<list1.size();l++){
		              if(lemmaMap.containsKey(tokens[j])){
		              List<String> list2 = lemmaMap.get(tokens[j]);
		              for(int k = 0;k<list2.size();k++){
		                 //MyMap map1 = new MyMap();
		                if(lemmaMap.containsKey(tokens[m])){
		              List<String> list3 = lemmaMap.get(tokens[m]);
		              for(int n = 0;n<list3.size();n++){
                                                      StringBuilder sb = new StringBuilder();

		              	    sb.append(list1.get(l)+","+list2.get(k)+","+list3.get(n)+"#"+pos1);
                			list.add(sb.toString());

		              //  pair.set(list1.get(l)+","+list2.get(k)+","+list3.get(n));
		              }
		              }
		              else{
                                                        StringBuilder sb = new StringBuilder();

		              	 sb.append(list1.get(l)+","+list2.get(k)+","+tokens[m]+"#"+pos1);
                                 list.add(sb.toString());
		 				}
		 			}
		 		}
		 		else{
              if(lemmaMap.containsKey(tokens[m])){
              List<String> list3 = lemmaMap.get(tokens[m]);
              for(int n = 0;n<list3.size();n++){
                                      StringBuilder sb = new StringBuilder();

               sb.append(list1.get(l)+","+tokens[j]+","+list3.get(n)+"#"+pos1);
               list.add(sb.toString());
                // pair.set(list1.get(l)+","+tokens[j]+","+list3.get(n));
              }
              }
              else{
                                      StringBuilder sb = new StringBuilder();

                sb.append(list1.get(l)+","+tokens[j]+","+tokens[m]+"#"+pos1);
                list.add(sb.toString());
              }
            }
            }
          }
           else{
          if(lemmaMap.containsKey(tokens[j])){
              List<String> list2 = lemmaMap.get(tokens[j]);
              for(int k = 0;k<list2.size();k++){
                 //MyMap map1 = new MyMap();
                if(lemmaMap.containsKey(tokens[m])){
              List<String> list3 = lemmaMap.get(tokens[m]);
              for(int n = 0;n<list3.size();n++){
                                      StringBuilder sb = new StringBuilder();

              	sb.append(tokens[i]+","+list2.get(k)+","+list3.get(n)+"#"+pos1);
                list.add(sb.toString());
                //pair.set(tokens[i]+","+list2.get(k)+","+list3.get(n));
              }
              }
              else{
                                        StringBuilder sb = new StringBuilder();

                // pair.set(tokens[i]+","+list2.get(k)+","+tokens[m]);
                sb.append(tokens[i]+","+list2.get(k)+","+tokens[m]+"#"+pos1);
                list.add(sb.toString());
              }
              }
            }
            else{
                if(lemmaMap.containsKey(tokens[m])){
              List<String> list3 = lemmaMap.get(tokens[m]);
              for(int n = 0;n<list3.size();n++){
                                      StringBuilder sb = new StringBuilder();

                //pair.set(tokens[i]+","+tokens[j]+","+list3.get(n));
                sb.append(tokens[i]+","+tokens[j]+","+list3.get(n)+"#"+pos1);
                list.add(sb.toString());
              }
              }
              else{
                                        StringBuilder sb = new StringBuilder();

                // pair.set(tokens[i]+","+tokens[j]+","+tokens[m]);
                sb.append(tokens[i]+","+tokens[j]+","+tokens[m]+"#"+pos1);
                list.add(sb.toString());
                }
            }

                }
                }
            }
        }
    }//end all 3 for loops
          

          return list.iterator();
      }
    });

   
        JavaPairRDD<String, String> wordPairs = words.mapToPair(new PairFunction<String, String, String>() {
      public Tuple2<String, String> call(String s) {
          
          String pos = s.split("#")[1];
          String toEmit = s.split("#")[0];
        return new Tuple2<String, String>(toEmit, pos);
      }
    });



    JavaPairRDD<String, String> wordCount = wordPairs.reduceByKey(new Function2<String, String, String>() {
      @Override
      public String call(String integer, String integer2) throws Exception {
          StringBuilder sb = new StringBuilder();
          sb.append(integer + "  "+integer2);
//        return integer + "  "+ integer2;
          return sb.toString();
      }
    }).sortByKey();
    

JavaRDD<String> out = wordCount.map(new Function<Tuple2<String,String>,String>(){
@Override
public String call(Tuple2<String,String> tup){
          int count = tup._2.split("<").length  - 1;

return tup._1 + " : "+tup._2 + "  Count = "+count;
}

});

out.coalesce(1).saveAsTextFile(args[1]);

  }
}