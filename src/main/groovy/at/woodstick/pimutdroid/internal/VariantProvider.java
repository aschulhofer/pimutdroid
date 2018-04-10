package at.woodstick.pimutdroid.internal;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.plugins.ExtensionContainer;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.FeatureExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;

public class VariantProvider {

	private ExtensionContainer extensionContainer;

	public VariantProvider(ExtensionContainer extensionContainer) {
		this.extensionContainer = extensionContainer;
	}
	
	public Collection<AndroidVariant> getVariantsFrom(BaseExtension androidExtension) {
		Optional<AppExtension> appExtension = getAndroidExtension(AppExtension.class);
		if(appExtension.isPresent()) {
			return asBaseVariantCollection(appExtension.get().getApplicationVariants());
		}
		
		Optional<LibraryExtension> libraryExtension = getAndroidExtension(LibraryExtension.class);
		if(libraryExtension.isPresent()) {
			return asBaseVariantCollection(libraryExtension.get().getLibraryVariants());
		}
		
		Optional<FeatureExtension> featureExtension = getAndroidExtension(FeatureExtension.class);
		if(featureExtension.isPresent()) {
			return asBaseVariantCollection(featureExtension.get().getFeatureVariants());
		}
		
		throw new GradleException("No valid android extension applied.");
	}
	
	protected Collection<AndroidVariant> asBaseVariantCollection(DomainObjectSet<? extends BaseVariant> variants) {
		return variants.stream().map(baseVariant -> {
			return new AndroidVariant(baseVariant);
		}).collect(Collectors.toList());
	}
	
	protected <T> Optional<T> getAndroidExtension(Class<T> clazz) {
		T extension = extensionContainer.findByType(clazz);
		return Optional.ofNullable(extension);
	}
	
	public static VariantProvider withExtensionContainer(ExtensionContainer extensionContainer) {
		return new VariantProvider(extensionContainer);
	}
}
