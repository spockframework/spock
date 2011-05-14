/*
 * pgp-gradle-plugin
 * Copyright (C) 2010 Joern Huxhorn
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2010 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.gradle.pgp 

import org.gradle.api.*
import org.gradle.api.logging.*
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import org.gradle.api.artifacts.maven.MavenDeployer

/**
 * This class requires Gradle ff3496fd63d39769d2d2df154ae373493c504f41
 * Fri Nov 05 2010 05:49:51 GMT+0100 (CET) or newer
 */
class PgpPlugin implements Plugin<Project> {
	def logger = Logging.getLogger(this.class)
	
	/**
	 * Static so password is only requested once if keyId isn't changed in
	 * sub-projects.
	 */
	private static final PgpSigner signer;
	
	static {
		signer=new PgpSigner();
		def logger = Logging.getLogger(PgpPlugin)
		if(logger.isDebugEnabled()) logger.debug('Created PgpSigner instance.')
	}
	
	def void apply(Project project) {
        project.convention.plugins.pgp = new PgpPluginConvention()
        def uploadTask = project.getByName('uploadArchives')
        uploadTask.repositories.withType(MavenDeployer).allObjects { repository ->
			repository.beforeDeployment { mavenDeployment ->
				def secretKeyRingFile = project.convention.plugins.pgp.secretKeyRingFile
				def keyId = project.convention.plugins.pgp.keyId
				if(!secretKeyRingFile || !secretKeyRingFile.isFile()
					&& project.hasProperty('pgpSecretKeyRingFile')) {
					secretKeyRingFile = new File(project.pgpSecretKeyRingFile)
				}
				if(!secretKeyRingFile) {
					throw new InvalidUserDataException("Missing secretKeyRingFile! Specify using convention 'pgp { secretKeyRingFile = file }' or gradle property '-PpgpSecretKeyRingFile=file'.")
				}
				if(!secretKeyRingFile.isFile()) {
					throw new InvalidUserDataException("Invalid secretKeyRingFile '$secretKeyRingFile.absolutePath'! Specify using convention 'pgp { secretKeyRingFile = file }' or gradle property '-PpgpSecretKeyRingFile=file'.")
				}
	
				if(!keyId && project.hasProperty('pgpKeyId')) {
					keyId = project.pgpKeyId
				}
				if(!keyId) {
					throw new InvalidUserDataException("Missing keyId! Specify using convention 'pgp { keyId = 'cafebabe' }' or gradle property '-PpgpKeyId=cafebabe'.")
				}
				keyId = keyId.toLowerCase() // keyIds are always referred to in lower-case

				boolean resetPassword = false
				signer.setSecretKeyRingFile( secretKeyRingFile )
	
				if(keyId != signer.keyId) {
					if(logger.isDebugEnabled()) logger.debug('keyId={}, signer.keyId={} => resetPassword=true', keyId, signer.keyId)
					signer.keyId = keyId
					resetPassword = true
					// reset signer password if keyId changes
					// this enables signing with different keys in submodules
				}
				
				if(resetPassword) {
					if(logger.isDebugEnabled()) logger.debug('(Re)Setting password to value from pgp convention.')
					signer.password = project.convention.plugins.pgp.password
				}
	
				if(!signer.password && project.hasProperty('pgpPassword')) {
					if(logger.isDebugEnabled()) logger.debug('Setting password to value from project property \'pgpPassword\'')
					signer.password = project.pgpPassword
				}
	
				// HACK - hardcoded the specific configuration we use for artifacts in Geb.
				def confs = ["default", "meta"].collect { project.configurations.getByName(it) }
				def artifacts = confs*.allArtifactFiles*.files.flatten()
				
				List<File> allFiles=new ArrayList<File>()
				List<File> signFiles=new ArrayList<File>(artifacts)
				signFiles.add(mavenDeployment.pomArtifact.file)
				for(File file in signFiles) {
					if(logger.isInfoEnabled())
						logger.info("Creating signature(s) for file '${file.absolutePath}'...")
					List<File> files = signer.sign(file)
					if(files)
					{
						allFiles.addAll(files);
						if(logger.isDebugEnabled()) logger.debug('Created signature files \'{}\'...', files)
					}
				}
				if(logger.isInfoEnabled()) logger.info('Created signature files {}.', allFiles)
	
				def findClassifierPattern = /(.+?)-$project.version(?:-(.+))?\.(\w+\.asc)/
	
				for(File file in allFiles)
				{
					def filename = file.name
					if(filename.endsWith('.xml.asc')) {
						DefaultPublishArtifact artifact = new DefaultPublishArtifact(filename, 'Artifact Signature', 'pom.asc', null, new Date(), file, this) 
						mavenDeployment.addArtifact(artifact)
						if(logger.isDebugEnabled()) logger.debug('Added artifact: {}', artifact)
					} else {
						assert filename ==~ findClassifierPattern
						// xlson saved the day again ;)
						(filename =~ findClassifierPattern).find { full, basename, classifier, fileEnding ->
							if(logger.isDebugEnabled()) {
								logger.debug('Full: {}', full)
								logger.debug('Basename: {}', basename)
								logger.debug('Classifier: {}', classifier)
								logger.debug('File ending: {}', fileEnding)
							}
							DefaultPublishArtifact artifact = new DefaultPublishArtifact(full, 'Artifact Signature', fileEnding, classifier, new Date(), file, this) 
							mavenDeployment.addArtifact(artifact)
							if(logger.isDebugEnabled()) logger.debug('Added artifact: {}', artifact)
						}
					}
				}
			}
		}
	}
}

class PgpPluginConvention {
	File secretKeyRingFile
	String keyId
	String password
	
	def pgp(Closure closure) {
        closure.delegate = this
        closure() 
    }

}

