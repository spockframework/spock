package common.extension.spock.runtime;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.ISkippable;
import org.spockframework.runtime.model.SpecInfo;
import common.extension.spock.lang.Tags;

/**
 * User: gcurrey
 * Date: 12/12/13
 * Time: 9:33 AM
 */
public class TagsExtension extends AbstractAnnotationDrivenExtension<Tags> {
    public void visitSpecAnnotation(Tags annotation, SpecInfo spec) {
        doVisit(annotation, spec);
    }

    public void visitFeatureAnnotation(Tags annotation, FeatureInfo feature) {
        doVisit(annotation, feature);
    }

    private void doVisit(Tags annotation, ISkippable skippable) {
        String[] tags = annotation.value();
        String[] suppliedTags = System.getProperty("tags") != null ? System.getProperty("tags").split(",") : null;

        boolean execute = false;
        if (suppliedTags == null || suppliedTags.length == 0){
            execute = true;
        } else {
            for(String tag : tags){
                execute = true;
            }
        }

        skippable.setSkipped(!execute);
    }
}
