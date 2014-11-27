import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.io.File;
import java.io.FileNotFoundException;

import org.zeroxlab.owl.IMatcher;
import org.zeroxlab.owl.MatchResult;
import org.zeroxlab.owl.TemplateNotFoundException;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

interface IServiceManager{
    public int getService(String name);
}
abstract class ServiceManagerNative implements IServiceManager{
    public static void test(){
        System.out.println("ServiceManagerNative.test");
    }
}
public class Test {
	    /**
     * @param args
     * @throws TemplateNotFoundException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException,
            TemplateNotFoundException {

        ServiceManagerNative.test();
        // break;
        //
        // System.out.println("Started");
        // String haystack = "/home/liuyq/workspace/Aster/classes/logo.png";
        // IplImage img = cvLoadImage(haystack);
        // System.out.println("Finished");
        // if (img == null){
        // System.out.println("haystack=" + haystack );
        // }
        //
        // Properties props = System.getProperties();
        // Iterator<Object> keyIt = props.keySet().iterator();
        // while(keyIt.hasNext()){
        // String key = keyIt.next().toString();
        // String value = props.getProperty(key);
        // System.out.println(key+"="+value);
        // }
        //
        // System.out.println("===============");
        // Map<String, String> env = System.getenv();
        // for (Entry<String, String> en : env.entrySet()){
        // System.out.println(en.getKey() + "="+en.getValue());
        // }
	}

    public static void test() throws FileNotFoundException {
           /* TemplateNotFoundException {
        String fullScreen = "/tmp/workspace/test//owl.png";
        String needle = "/tmp/workspace/test/0.png";
        double similarity = 0.9;
        PyramidTemplateMatcher matcher = new PyramidTemplateMatcher();
        MatchResult r = dispatch(matcher, fullScreen, needle, similarity);*/
//        System.out.println("end");
//        System.out.println("r.cx()" + "=" + r.cx() + ",r.cy()=" + r.cy());
    }
    public static MatchResult dispatch(IMatcher matcher, String haystack,
            String needle, double similarity) throws FileNotFoundException,
            TemplateNotFoundException {

        // Properties props = System.getProperties();
        // Iterator<Object> keyIt = props.keySet().iterator();
        // while (keyIt.hasNext()) {
        // String key = keyIt.next().toString();
        // String value = props.getProperty(key);
        // System.out.println(key + "=" + value);
        // }
        // System.out.println("===============");
        // Map<String, String> env = System.getenv();
        // for (Entry<String, String> en : env.entrySet()) {
        // System.out.println(en.getKey() + "=" + en.getValue());
        // }

        needle = (new File(needle)).getAbsolutePath();

        IplImage img = cvLoadImage(haystack);

        if (img == null) {
            System.out.println("haystack=" + haystack);
            throw new FileNotFoundException("can't open `" + haystack + "'");
        }
        IplImage tmpl = cvLoadImage(needle);
        if (tmpl == null) {
            System.out.println("needle=" + needle);
            throw new FileNotFoundException("can't open `" + needle + "'");
        }
        MatchResult result = matcher.find(img, tmpl, similarity);
        return result;
    }
}
