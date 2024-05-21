/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.utils;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Moretti_Lu
 */
public class XmlValidationEventHandler implements ValidationEventHandler {

    private final Logger logger = LoggerFactory.getLogger(XmlValidationEventHandler.class);

    /**
     * Holds the first error validation event.
     */
    private ValidationEvent firstErrorValidationEvent;

    /**
     * @return Always returns <code>true</code>, because we want to continue the current unmarshal, validate, or marshal
     *         to log all events.
     */
    @Override
    public boolean handleEvent(ValidationEvent event) {
        if (firstErrorValidationEvent == null && event.getSeverity() > ValidationEvent.WARNING) {
            firstErrorValidationEvent = event;
        }

        switch (event.getSeverity()) {
        case ValidationEvent.WARNING:
            logger.warn("Validation warning: {}", event);
            break;
        case ValidationEvent.ERROR:
        case ValidationEvent.FATAL_ERROR:
            logger.error("Validation error: {}", event);
            break;
        default:
            logger.info("Validation event: {}", event);
        }

        return false;
    }

    public ValidationEvent getFirstErrorValidationEvent() {
        return firstErrorValidationEvent;
    }
}
