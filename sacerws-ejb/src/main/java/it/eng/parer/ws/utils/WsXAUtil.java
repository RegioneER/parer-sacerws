package it.eng.parer.ws.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xadisk.additional.XAFileInputStreamWrapper;
import org.xadisk.additional.XAFileOutputStreamWrapper;
import org.xadisk.bridge.proxies.interfaces.XADiskBasicIOOperations;
import org.xadisk.bridge.proxies.interfaces.XAFileInputStream;
import org.xadisk.bridge.proxies.interfaces.XAFileOutputStream;
import org.xadisk.filesystem.exceptions.DirectoryNotEmptyException;
import org.xadisk.filesystem.exceptions.FileAlreadyExistsException;
import org.xadisk.filesystem.exceptions.FileNotExistsException;
import org.xadisk.filesystem.exceptions.FileUnderUseException;
import org.xadisk.filesystem.exceptions.InsufficientPermissionOnFileException;
import org.xadisk.filesystem.exceptions.LockingFailedException;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsException;

/**
 *
 * @author Bonora_L
 */
public class WsXAUtil {

    private static final Logger log = LoggerFactory.getLogger(WsXAUtil.class);

    private WsXAUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static OutputStream createFileOS(XADiskBasicIOOperations session, File file, boolean createFile)
            throws SacerWsException {
        try {
            if (createFile) {
                session.createFile(file, false);
            }
            XAFileOutputStream xafos;
            xafos = session.createXAFileOutputStream(file, true);
            return new XAFileOutputStreamWrapper(xafos);
        } catch (FileNotExistsException | FileUnderUseException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException | FileAlreadyExistsException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);

        } catch (InterruptedException e) {
            log.warn("Interrupted createFileOS!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static InputStream createFileIS(XADiskBasicIOOperations session, File file, boolean createFile)
            throws SacerWsException {
        try {
            if (createFile) {
                session.createFile(file, false);
            }
            XAFileInputStream xafis;
            xafis = session.createXAFileInputStream(file);
            return new XAFileInputStreamWrapper(xafis);
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);

        } catch (InterruptedException e) {
            log.warn("Interrupted createFileIS!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static void createDirectory(XADiskBasicIOOperations session, File dir) throws SacerWsException {
        try {
            session.createFile(dir, true);
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);

        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static File[] listFiles(XADiskBasicIOOperations session, File root) throws SacerWsException {
        try {

            String[] filesName = session.listFiles(root);
            File[] files = new File[filesName.length];
            for (int i = 0; i < filesName.length; i++) {
                files[i] = new File(root, filesName[i]);
            }
            return files;
        } catch (FileNotExistsException | LockingFailedException | NoTransactionAssociatedException
                | InsufficientPermissionOnFileException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted listFiles!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static void moveFile(XADiskBasicIOOperations session, File file, File dest) throws SacerWsException {
        try {
            session.moveFile(file, dest);
        } catch (FileNotExistsException | LockingFailedException | NoTransactionAssociatedException
                | InsufficientPermissionOnFileException | FileAlreadyExistsException | FileUnderUseException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted moveFile!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static void copyFile(XADiskBasicIOOperations session, File file, File dest) throws SacerWsException {
        try {
            session.copyFile(file, dest);
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static void deleteFile(XADiskBasicIOOperations session, File file) throws SacerWsException {

        try {
            session.deleteFile(file);
        } catch (DirectoryNotEmptyException | FileNotExistsException | FileUnderUseException
                | InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted deleteFile!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }

    }

    public static boolean fileExistsAndIsDirectory(XADiskBasicIOOperations session, File file) throws SacerWsException {
        try {
            return session.fileExistsAndIsDirectory(file);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted fileExistsAndIsDirectory!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }

    }

    public static boolean fileExistsAndIsDirectoryLockExclusive(XADiskBasicIOOperations session, File file)
            throws SacerWsException {
        try {
            return session.fileExistsAndIsDirectory(file, true);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }

    }

    public static boolean fileExists(XADiskBasicIOOperations session, File file) throws SacerWsException {
        try {
            return session.fileExists(file);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            log.warn("Interrupted fileExists!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static long getFileLength(XADiskBasicIOOperations session, File file) throws SacerWsException {
        try {
            return session.getFileLength(file);
        } catch (FileNotExistsException | LockingFailedException | NoTransactionAssociatedException
                | InsufficientPermissionOnFileException e) {
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new SacerWsException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }

    }

    /**
     * Metodo statico che rimuove ricorsivamente i dati contenuti in una directory in transazione
     *
     * @param session
     *            la sessione in transazione
     * @param dirPath
     *            la directory
     * 
     * @throws SacerWsException
     *             errore generico
     */
    public static void rimuoviFileRicorsivamente(XADiskBasicIOOperations session, File dirPath)
            throws SacerWsException {
        File[] elencoFile = WsXAUtil.listFiles(session, dirPath);
        if (elencoFile.length > 0) {
            for (File tmpFile : elencoFile) {
                if (WsXAUtil.fileExistsAndIsDirectory(session, tmpFile)) {
                    rimuoviFileRicorsivamente(session, tmpFile);
                } else {
                    WsXAUtil.deleteFile(session, tmpFile);
                }
            }
            WsXAUtil.deleteFile(session, dirPath);
        }
    }
}
