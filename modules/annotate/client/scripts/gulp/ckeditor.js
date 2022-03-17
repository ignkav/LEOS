/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
var gulp = require('gulp');
var path = require('path');


var ckeditor4Files = [
  {name: 'ckeditor-custom-config', src: './leos/sidebar/ckeditor/ckeditor-config.js', target: 'scripts'},
  {name: 'ckeditor-full', src: './node_modules/ckeditor4/**/*.{js,css,png}', target: 'ckeditor'},
  {name: 'ckeditor-autolink-plugin', src: './leos/sidebar/ckeditor/autolink/plugin.js', target: 'ckeditor/plugins/autolink'},
  {name: 'ckeditor-textmatch-plugin', src: './leos/sidebar/ckeditor/textmatch/plugin.js', target: 'ckeditor/plugins/textmatch'},
];

/**
 * Copies the files required for the ckeditor to the directory specified
 * by baseDirectory. Additionaly it created the folder structure as expected
 * by the ckeditor.
 * @param {string} taskName
 * @param {string} baseDirectory 
 */
function createCkEditorFilesTask(taskName, baseDirectory) {

  let copyTasks = createCopyTasks(baseDirectory);
  
  copyTasks.forEach(task => {
    gulp.task(task.name, task.copyToSubDirectoryStructure);
  });

  let taskNames = ckeditor4Files.map(ckfile => ckfile.name);

  gulp.task(taskName, taskNames);
}

function createCopyTasks(baseDirectory) {
  
  return ckeditor4Files.map((ckfile) => {
    
    return {
      name: ckfile.name,
      copyToSubDirectoryStructure: () => {
        let targetDirectory = path.join(baseDirectory, ckfile.target);
        
        return gulp.src(ckfile.src)
          .pipe(gulp.dest(targetDirectory));
      }
    };
    

  });
}

module.exports = createCkEditorFilesTask;
