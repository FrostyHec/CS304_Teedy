package com.sismics.docs.core.dao;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DocumentUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.constant.PermType;
import com.sismics.util.context.ThreadLocalContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * Tests the document DAO.
 *
 * @author codex
 */
public class TestDocumentDao extends BaseTransactionalTest {
    @Test
    public void testCreateUpdateAndCount() throws Exception {
        User user = createUser("testDocumentDao");
        File file = createFile(user, FILE_JPG_SIZE);
        DocumentDao documentDao = new DocumentDao();

        long documentCountBefore = documentDao.getDocumentCount();

        Document document = new Document();
        document.setUserId(user.getId());
        document.setLanguage("eng");
        document.setTitle("Initial title");
        document.setDescription("Initial description");
        document.setSubject("Initial subject");
        document.setIdentifier("Initial identifier");
        document.setPublisher("Initial publisher");
        document.setFormat("Initial format");
        document.setSource("Initial source");
        document.setType("Initial type");
        document.setCoverage("Initial coverage");
        document.setRights("Initial rights");
        document.setCreateDate(new Date(1_600_000_000_000L));

        String documentId = documentDao.create(document, user.getId());
        Assert.assertNotNull(documentId);
        Assert.assertEquals(documentId, document.getId());

        Document storedDocument = documentDao.getById(documentId);
        Assert.assertNotNull(storedDocument);
        Assert.assertEquals(user.getId(), storedDocument.getUserId());
        Assert.assertEquals("Initial title", storedDocument.getTitle());
        Assert.assertEquals("eng", storedDocument.getLanguage());
        Assert.assertNotNull(storedDocument.getUpdateDate());

        Document fileUpdatedDocument = new Document();
        fileUpdatedDocument.setId(documentId);
        fileUpdatedDocument.setFileId(file.getId());
        documentDao.updateFileId(fileUpdatedDocument);
        ThreadLocalContext.get().getEntityManager().clear();

        storedDocument = documentDao.getById(documentId);
        Assert.assertNotNull(storedDocument);
        Assert.assertEquals(file.getId(), storedDocument.getFileId());

        Document updatedDocument = new Document();
        updatedDocument.setId(documentId);
        updatedDocument.setUserId(user.getId());
        updatedDocument.setLanguage("fra");
        updatedDocument.setTitle("Updated title");
        updatedDocument.setDescription("Updated description");
        updatedDocument.setSubject("Updated subject");
        updatedDocument.setIdentifier("Updated identifier");
        updatedDocument.setPublisher("Updated publisher");
        updatedDocument.setFormat("Updated format");
        updatedDocument.setSource("Updated source");
        updatedDocument.setType("Updated type");
        updatedDocument.setCoverage("Updated coverage");
        updatedDocument.setRights("Updated rights");
        updatedDocument.setCreateDate(new Date(1_600_000_100_000L));
        updatedDocument.setFileId(file.getId());

        Document updatedStoredDocument = documentDao.update(updatedDocument, user.getId());
        Assert.assertEquals("Updated title", updatedStoredDocument.getTitle());
        Assert.assertEquals("fra", updatedStoredDocument.getLanguage());
        Assert.assertEquals(file.getId(), updatedStoredDocument.getFileId());

        ThreadLocalContext.get().getEntityManager().clear();
        storedDocument = documentDao.getById(documentId);
        Assert.assertNotNull(storedDocument);
        Assert.assertEquals("Updated title", storedDocument.getTitle());
        Assert.assertEquals("Updated description", storedDocument.getDescription());
        Assert.assertEquals("Updated subject", storedDocument.getSubject());
        Assert.assertEquals("Updated identifier", storedDocument.getIdentifier());
        Assert.assertEquals("Updated publisher", storedDocument.getPublisher());
        Assert.assertEquals("Updated format", storedDocument.getFormat());
        Assert.assertEquals("Updated source", storedDocument.getSource());
        Assert.assertEquals("Updated type", storedDocument.getType());
        Assert.assertEquals("Updated coverage", storedDocument.getCoverage());
        Assert.assertEquals("Updated rights", storedDocument.getRights());
        Assert.assertEquals("fra", storedDocument.getLanguage());
        Assert.assertEquals(file.getId(), storedDocument.getFileId());
        Assert.assertEquals(1_600_000_100_000L, storedDocument.getCreateDate().getTime());

        ThreadLocalContext.get().getEntityManager().flush();
        Assert.assertEquals(documentCountBefore + 1, documentDao.getDocumentCount());

        TransactionUtil.commit();
    }

    @Test
    public void testGetDocument() throws Exception {
        User user = createUser("testGetDocument");
        DocumentDao documentDao = new DocumentDao();

        Document document = new Document();
        document.setUserId(user.getId());
        document.setLanguage("eng");
        document.setTitle("Visible document");
        document.setCreateDate(new Date(1_600_000_200_000L));
        DocumentUtil.createDocument(document, user.getId());

        Assert.assertNull(documentDao.getDocument(document.getId(), PermType.READ, List.of()));

        Assert.assertNotNull(documentDao.getDocument(document.getId(), PermType.READ, List.of(user.getId())));
    }

    @Test
    public void testGetDocumentNoResult() throws Exception {
        User user = createUser("testGetDocumentNoResult");
        DocumentDao documentDao = new DocumentDao();

        Document document = new Document();
        document.setUserId(user.getId());
        document.setLanguage("eng");
        document.setTitle("Deleted document");
        document.setCreateDate(new Date(1_600_000_300_000L));
        DocumentUtil.createDocument(document, user.getId());

        ThreadLocalContext.get().getEntityManager()
                .createQuery("update Document d set d.deleteDate = :date where d.id = :id")
                .setParameter("date", new Date())
                .setParameter("id", document.getId())
                .executeUpdate();
        ThreadLocalContext.get().getEntityManager().clear();

        Assert.assertNull(documentDao.getDocument(document.getId(), PermType.READ, List.of(user.getId())));
    }
}
