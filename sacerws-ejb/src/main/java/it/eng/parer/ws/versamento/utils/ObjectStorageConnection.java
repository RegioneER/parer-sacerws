/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versamento.utils;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Metodo alternativo alla cache dei client. Usa il concetto della connection un po' come fa jdbc.
 *
 * @author Snidero_L
 */
public class ObjectStorageConnection implements AutoCloseable {

    private final Logger log = LoggerFactory.getLogger(ObjectStorageConnection.class);
    private final S3Client s3Client;

    /**
     * Creazione del client per il collegamento all'object storage
     *
     * @param storageAddress indirizzo dell'object storage scelto
     * @param accessKeyId    access key id S3
     * @param secretKey      secret key S3
     */
    public ObjectStorageConnection(String storageAddress, String accessKeyId, String secretKey) {
	this(URI.create(storageAddress), accessKeyId, secretKey);
    }

    /**
     * Creazione del client per il collegamento all'object storage
     *
     * @param storageAddress indirizzo dell'object storage scelto (come URI)
     * @param accessKeyId    access key id S3
     * @param secretKey      secret key S3
     */
    public ObjectStorageConnection(URI storageAddress, String accessKeyId, String secretKey) {
	// create basic credentials

	final AwsCredentialsProvider credProvider = StaticCredentialsProvider
		.create(AwsBasicCredentials.create(accessKeyId, secretKey));

	log.info("Sto per effettuare il collegamento all'endpoint S3 [{}] dal thread {}",
		storageAddress, Thread.currentThread().getName());
	s3Client = S3Client.builder().endpointOverride(storageAddress).region(Region.US_EAST_1)
		.credentialsProvider(credProvider).build();
    }

    /**
     * Client S3 già configurato durante la costruzione dell'oggetto
     *
     * @return client S3
     */
    public S3Client getS3Client() {
	return s3Client;
    }

    @Override
    public void close() throws Exception {
	log.info("Close endpoint S3 dal thread {}", Thread.currentThread().getName());
	if (s3Client != null) {
	    s3Client.close();
	}

    }

}
