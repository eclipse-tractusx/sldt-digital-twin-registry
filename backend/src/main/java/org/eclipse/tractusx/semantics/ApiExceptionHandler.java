/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.eclipse.tractusx.semantics;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.aas.registry.model.Message;
import org.eclipse.tractusx.semantics.aas.registry.model.Result;
import org.eclipse.tractusx.semantics.registry.service.EntityNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

   @Override
   protected ResponseEntity<Object> handleMethodArgumentNotValid( final MethodArgumentNotValidException ex,
         final HttpHeaders headers,
         final HttpStatusCode status, final WebRequest request ) {
      // TODO: the ErrorResponse classes are currently in the AAS api definition
      // we should move that out to a general api definition. Error response should be identical for all semantic layer
      // services.
      List<Message> messages = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map( fieldError -> new Message()
                  .code( fieldError.getField() )
                  .messageType( Message.MessageTypeEnum.ERROR )
                  .text( Optional.ofNullable( fieldError.getDefaultMessage() ).orElseGet( () -> "null" ) )
            ).collect( Collectors.toList() );
      return new ResponseEntity<>(
            new Result().messages( messages ), HttpStatus.BAD_REQUEST );
   }

   @ExceptionHandler( { EntityNotFoundException.class } )
   public ResponseEntity<Object> handleNotFoundException( final HttpServletRequest request,

         final RuntimeException exception ) {
      return new ResponseEntity<>(
            new Result().messages( List.of( new Message().messageType( Message.MessageTypeEnum.ERROR ).text( exception.getMessage() ) ) ),
            HttpStatus.NOT_FOUND );
   }

   @ExceptionHandler( { IllegalArgumentException.class } )
   @ResponseStatus( HttpStatus.BAD_REQUEST )
   public ResponseEntity<Object> handleIllegalArgumentException( final IllegalArgumentException exception ) {
      return new ResponseEntity<>(
            new Result().messages( List.of( new Message().messageType( Message.MessageTypeEnum.ERROR ).text( exception.getMessage() ) ) ),
            HttpStatus.BAD_REQUEST );
   }

   @ExceptionHandler( { MethodArgumentConversionNotSupportedException.class } )
   @ResponseStatus( HttpStatus.BAD_REQUEST )
   public ResponseEntity<Object> handleMethodArgumentNotSupportedException( final HttpServletRequest request ) {
      String queryString = request.getQueryString();
      return new ResponseEntity<>(
            new Result().messages(
                  List.of( new Message().messageType( Message.MessageTypeEnum.ERROR )
                        .text( String.format( "The provided parameters are invalid. %s", URLDecoder.decode( queryString, StandardCharsets.UTF_8 ) ) ) ) ),
            HttpStatus.BAD_REQUEST );
   }

   @ExceptionHandler( { DuplicateKeyException.class } )
   @ResponseStatus( HttpStatus.BAD_REQUEST )
   public ResponseEntity<Object> handleDuplicateKeyException( DuplicateKeyException duplicateKeyException ) {
      return new ResponseEntity<>(
            new Result().messages( List.of( new Message().messageType( Message.MessageTypeEnum.ERROR ).text(duplicateKeyException.getMessage() ) ) ),
            HttpStatus.BAD_REQUEST );
   }

}
