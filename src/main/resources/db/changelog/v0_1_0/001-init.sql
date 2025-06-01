-- liquibase formatted sql

-- changeset app-dev:create-user-table
CREATE TABLE app_user (
                          keycloak_user_id VARCHAR(255) NOT NULL,
                          first_name VARCHAR(100),
                          last_name VARCHAR(100),
                          email VARCHAR(255) NOT NULL,
                          role VARCHAR(50) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          lives_remaining INTEGER,
                          CONSTRAINT pk_app_user PRIMARY KEY (keycloak_user_id),
                          CONSTRAINT uq_user_email UNIQUE (email),
                          CONSTRAINT chk_user_role CHECK (role IN ('TUTOR', 'STUDENT'))
);
-- rollback DROP TABLE app_user;

-- changeset app-dev:create-class-table
CREATE TABLE class_entity (
                              id UUID NOT NULL DEFAULT uuid_generate_v7(),
                              name VARCHAR(255) NOT NULL,
                              description TEXT,
                              tutor_id VARCHAR(255) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT pk_class_entity PRIMARY KEY (id),
                              CONSTRAINT fk_class_tutor FOREIGN KEY (tutor_id) REFERENCES app_user (keycloak_user_id)
);
-- rollback DROP TABLE class_entity;

-- changeset app-dev:create-class-student-join-table
CREATE TABLE class_student (
                               class_id UUID NOT NULL,
                               student_id VARCHAR(255) NOT NULL,
                               CONSTRAINT pk_class_student PRIMARY KEY (class_id, student_id),
                               CONSTRAINT fk_classstudent_class FOREIGN KEY (class_id) REFERENCES class_entity (id) ON DELETE CASCADE,
                               CONSTRAINT fk_classstudent_student FOREIGN KEY (student_id) REFERENCES app_user (keycloak_user_id) ON DELETE CASCADE
);
-- rollback DROP TABLE class_student;

-- changeset app-dev:create-lesson-table
CREATE TABLE lesson (
                        id UUID NOT NULL DEFAULT uuid_generate_v7(),
                        class_id UUID NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        start_time TIMESTAMPTZ NOT NULL,
                        end_time TIMESTAMPTZ NOT NULL,
                        is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
                        recurrence_rule VARCHAR(255),
                        status VARCHAR(50) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT pk_lesson PRIMARY KEY (id),
                        CONSTRAINT fk_lesson_class FOREIGN KEY (class_id) REFERENCES class_entity (id) ON DELETE CASCADE,
                        CONSTRAINT chk_lesson_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED'))
);
-- rollback DROP TABLE lesson;

-- changeset app-dev:create-lesson-file-attachment-table
CREATE TABLE lesson_file_attachment (
                                        id UUID NOT NULL DEFAULT uuid_generate_v7(),
                                        lesson_id UUID NOT NULL,
                                        file_name VARCHAR(255) NOT NULL,
                                        s3_file_key VARCHAR(1024) NOT NULL,
                                        file_type VARCHAR(100),
                                        uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT pk_lesson_file_attachment PRIMARY KEY (id),
                                        CONSTRAINT uq_lessonfile_s3key UNIQUE (s3_file_key),
                                        CONSTRAINT fk_lessonfile_lesson FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE CASCADE
);
-- rollback DROP TABLE lesson_file_attachment;

-- changeset app-dev:create-homework-table
CREATE TABLE homework (
                          id UUID NOT NULL DEFAULT uuid_generate_v7(),
                          lesson_id UUID NOT NULL,
                          title VARCHAR(255) NOT NULL,
                          description TEXT,
                          type VARCHAR(50) NOT NULL,
                          deadline TIMESTAMPTZ NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT pk_homework PRIMARY KEY (id),
                          CONSTRAINT fk_homework_lesson FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE CASCADE,
                          CONSTRAINT chk_homework_type CHECK (type IN ('FILE_UPLOAD', 'TEST'))
);
-- rollback DROP TABLE homework;

-- changeset app-dev:create-test-table
CREATE TABLE test (
                      id UUID NOT NULL DEFAULT uuid_generate_v7(),
                      homework_id UUID NOT NULL,
                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT pk_test PRIMARY KEY (id),
                      CONSTRAINT uq_test_homework_id UNIQUE (homework_id),
                      CONSTRAINT fk_test_homework FOREIGN KEY (homework_id) REFERENCES homework (id) ON DELETE CASCADE
);
-- rollback DROP TABLE test;

-- changeset app-dev:create-question-table
CREATE TABLE question (
                          id UUID NOT NULL DEFAULT uuid_generate_v7(),
                          test_id UUID NOT NULL,
                          question_text TEXT NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT pk_question PRIMARY KEY (id),
                          CONSTRAINT fk_question_test FOREIGN KEY (test_id) REFERENCES test (id) ON DELETE CASCADE
);
-- rollback DROP TABLE question;

-- changeset app-dev:create-answer-option-table
CREATE TABLE answer_option (
                               id UUID NOT NULL DEFAULT uuid_generate_v7(),
                               question_id UUID NOT NULL,
                               option_text TEXT NOT NULL,
                               is_correct BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT pk_answer_option PRIMARY KEY (id),
                               CONSTRAINT fk_answeroption_question FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE
);
-- rollback DROP TABLE answer_option;

-- changeset app-dev:create-homework-submission-table
CREATE TABLE homework_submission (
                                     id UUID NOT NULL DEFAULT uuid_generate_v7(),
                                     homework_id UUID NOT NULL,
                                     student_id VARCHAR(255) NOT NULL,
                                     submission_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     submitted_file_key VARCHAR(1024),
                                     submitted_file_name VARCHAR(255),
                                     test_answers JSONB,
                                     grade INTEGER,
                                     feedback TEXT,
                                     is_late BOOLEAN NOT NULL DEFAULT FALSE,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT pk_homework_submission PRIMARY KEY (id),
                                     CONSTRAINT fk_submission_homework FOREIGN KEY (homework_id) REFERENCES homework (id) ON DELETE CASCADE,
                                     CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES app_user (keycloak_user_id) ON DELETE CASCADE,
                                     CONSTRAINT uq_homework_submission_student_homework UNIQUE (homework_id, student_id),
                                     CONSTRAINT chk_submission_grade CHECK (grade IS NULL OR (grade >= 0 AND grade <= 100))
);
-- rollback DROP TABLE homework_submission;