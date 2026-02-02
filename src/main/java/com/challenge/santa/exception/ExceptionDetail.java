package com.challenge.santa.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class ExceptionDetail implements Serializable {
  private static final long serialVersionUID = 6161308073102216336L;

  private @Nullable String title;
  private int status;
  private @Nullable String detail;
}
