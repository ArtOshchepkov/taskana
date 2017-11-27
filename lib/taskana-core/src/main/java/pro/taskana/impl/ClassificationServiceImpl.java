package pro.taskana.impl;

import pro.taskana.ClassificationService;
import pro.taskana.TaskanaEngine;
import pro.taskana.exceptions.NotAuthorizedException;
import pro.taskana.impl.persistence.ClassificationQueryImpl;
import pro.taskana.impl.util.IdGenerator;
import pro.taskana.model.Classification;
import pro.taskana.model.mappings.ClassificationMapper;
import pro.taskana.persistence.ClassificationQuery;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of ClassificationService.
 */
public class ClassificationServiceImpl implements ClassificationService {

    private static final String ID_PREFIX_CLASSIFICATION = "CLI";
    public static final Date CURRENT_CLASSIFICATIONS_VALID_UNTIL = Date.valueOf("9999-12-31");
    private ClassificationMapper classificationMapper;
    private TaskanaEngine taskanaEngine;

    public ClassificationServiceImpl(TaskanaEngine taskanaEngine, ClassificationMapper classificationMapper) {
        super();
        this.taskanaEngine = taskanaEngine;
        this.classificationMapper = classificationMapper;
    }

    @Override
    public List<Classification> getClassificationTree() throws NotAuthorizedException {
        List<Classification> rootClassifications;
        rootClassifications = this.createClassificationQuery().parentClassification("").validUntil(CURRENT_CLASSIFICATIONS_VALID_UNTIL).list();
        return this.populateChildClassifications(rootClassifications);
    }

    private List<Classification> populateChildClassifications(List<Classification> classifications) throws NotAuthorizedException {
        List<Classification> children = new ArrayList<>();
        for (Classification classification : classifications) {
            List<Classification> childClassifications = this.createClassificationQuery().parentClassification(classification.getId()).validUntil(CURRENT_CLASSIFICATIONS_VALID_UNTIL).list();
            children.addAll(populateChildClassifications(childClassifications));
        }
        classifications.addAll(children);
        return classifications;
    }

    @Override
    public void addClassification(Classification classification) {
        classification.setId(IdGenerator.generateWithPrefix(ID_PREFIX_CLASSIFICATION));
        classification.setCreated(Date.valueOf(LocalDate.now()));

        this.setDefaultValues(classification);

        classificationMapper.insert(classification);
    }

    @Override
    public void updateClassification(Classification classification) {
        this.setDefaultValues(classification);

        Classification oldClassification = this.getClassification(classification.getId(), classification.getDomain());

        if (oldClassification == null) {
            classification.setId(IdGenerator.generateWithPrefix(ID_PREFIX_CLASSIFICATION));
            classification.setCreated(Date.valueOf(LocalDate.now()));
            classificationMapper.insert(classification);
            return;
        }

        // ! If you update an classification twice the same day,
        // the older version is valid from today until yesterday.
        if (!oldClassification.getDomain().equals(classification.getDomain())) {
            classification.setCreated(Date.valueOf(LocalDate.now()));
            classificationMapper.insert(classification);
        } else {
            oldClassification.setValidUntil(Date.valueOf(LocalDate.now().minusDays(1)));
            classificationMapper.update(oldClassification);
            classificationMapper.insert(classification);
        }
    }

    private void setDefaultValues(Classification classification) {
        classification.setValidFrom(Date.valueOf(LocalDate.now()));
        classification.setValidUntil(CURRENT_CLASSIFICATIONS_VALID_UNTIL);
        classification.setValidInDomain(true);
        if (classification.getServiceLevel() != null) {
            try {
                Duration.parse(classification.getServiceLevel());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid duration. Please use the format defined by ISO 8601");
            }
        }

        if (classification.getParentClassificationId() == classification.getId()) {
            throw new IllegalArgumentException("A classification can't be a parent to itself");
        }

        if (classification.getParentClassificationId() == null) {
            classification.setParentClassificationId("");
        }
        if (classification.getDomain() == null) {
            classification.setDomain("");
        }
    }

    @Override
    public List<Classification> getAllClassificationsWithId(String id, String domain) {
        return classificationMapper.getAllClassificationsWithId(id, domain);
    }

    @Override
    public Classification getClassification(String id, String domain) {
        Classification classification;
        if (domain == null) {
            classification = classificationMapper.findByIdAndDomain(id, "", CURRENT_CLASSIFICATIONS_VALID_UNTIL);
        } else {
            classification = classificationMapper.findByIdAndDomain(id, domain, CURRENT_CLASSIFICATIONS_VALID_UNTIL);
        }
        return classification;
    }

    @Override
    public ClassificationQuery createClassificationQuery() {
        return new ClassificationQueryImpl(taskanaEngine);
    }
}
