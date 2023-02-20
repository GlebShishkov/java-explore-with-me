package com.example.mainserver.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

//
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ErrorResponse objectWrongEnterExeption(final ItemUnvailableException e) {
//        return new ErrorResponse(e.getMessage());
//    }
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ErrorResponse objectWrongEnterExeption(final BadRequestException e) {
//        return new ErrorResponse(e.getMessage());
//    }
//
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ErrorResponse objectWrongEnterExeption(final EmailWrongException e) {
//        return new ErrorResponse(e.getMessage());
//    }



    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final EventNotFoundException e) {
        return new ErrorResponse(e.getMessage(), "BAD_REQUEST", "Incorrectly made request.",
                LocalDateTime.now());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse objectWrongEnterExeption(final WrongDateException e) {
        return new ErrorResponse(e.getMessage(), "FORBIDDEN", "For the requested operation the conditions are not met.",
                LocalDateTime.now());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final CompilationNotFounfExeption e) {
        return new ErrorResponse(e.getMessage(), "NOT_FOUND", "The required object was not found.",
                LocalDateTime.now());
    }



    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final WrongNameException e) {
        return new ErrorResponse(e.getMessage(), "BAD_REQUEST", "Incorrectly made request.",
                LocalDateTime.now());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final CategoryNotFounfExeption e) {
        return new ErrorResponse(e.getMessage(), "NOT_FOUND", "The required object was not found.",
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final WrongEventCreationException e) {
        return new ErrorResponse(e.getMessage(), "BAD_REQUEST", "Incorrectly made request.",
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final WrongTimeEventCreationException e) {
        return new ErrorResponse(e.getMessage(), "FORBIDDEN", "Incorrectly made request.",
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse objectWrongEnterExeption(final EventPublishedException e) {
        return new ErrorResponse(e.getMessage(), "BAD_REQUEST", "For the requested operation the conditions are not met.",
                LocalDateTime.now());
    }




}
