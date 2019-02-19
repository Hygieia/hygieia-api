package com.capitalone.dashboard.service;

import com.capitalone.dashboard.model.Collector;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.model.QScopeOwner;
import com.capitalone.dashboard.model.Scope;
import com.capitalone.dashboard.repository.CollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.ScopeRepository;
import com.querydsl.core.BooleanBuilder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ScopeServiceImpl implements ScopeService {

	private static final Pattern COMPILE_PATTERN = Pattern.compile("[<>]");
	private final ComponentRepository componentRepository;
	private final ScopeRepository scopeRepository;
	private final CollectorRepository collectorRepository;

	/**
	 * Default autowired constructor for repositories
	 * 
	 * @param componentRepository
	 *            Repository containing components used by the UI (populated by
	 *            UI)
	 * @param collectorRepository
	 *            Repository containing all registered collectors
	 * @param scopeRepository
	 *            Repository containing all scopes
	 */
	@Autowired
	public ScopeServiceImpl(ComponentRepository componentRepository,
			CollectorRepository collectorRepository,
			ScopeRepository scopeRepository) {
		this.componentRepository = componentRepository;
		this.scopeRepository = scopeRepository;
		this.collectorRepository = collectorRepository;
	}

	/**
	 * Retrieves all unique scopes
	 * 
	 * @return A data response list of type Scope containing all unique scopes
	 */
	@Override
	public List<Scope> getAllScopes() {
		// Get all available scopes
		List<Scope> scopes = scopeRepository.findByOrderByProjectPathDesc();

		scopes.forEach(scope -> {
			Collector collector = collectorRepository
					.findOne(scope.getCollectorId());
			scope.setCollector(collector);
		});

		return scopes;
	}

	/**
	 * Retrieves the scope information for a given scope source system ID
	 * 
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param scopeId
	 *            A given scope's source-system ID
	 * 
	 * @return A data response list of type Scope containing all data for a
	 *         given scope source-system ID
	 */
	@Override
	public DataResponse<List<Scope>> getScope(ObjectId componentId,
			String scopeId) {
		Component component = componentRepository.findOne(componentId);
		CollectorItem item = component.getCollectorItems()
				.get(CollectorType.AgileTool).get(0);
		QScopeOwner team = new QScopeOwner("team");
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(team.collectorItemId.eq(item.getId()));

		// Get one scope by Id
		List<Scope> scope = scopeRepository.getScopeById(scopeId);

		Collector collector = collectorRepository
				.findOne(item.getCollectorId());

		return new DataResponse<>(scope, collector.getLastExecuted());
	}

	/**
	 * Retrieves the scope information for a given scope source system ID
	 *
	 * @param collectorId
	 *
	 * @return projects
	 */
	@Override
	public List<Scope>  getScopesByCollector(ObjectId collectorId) {
		List<Scope> scopes = scopeRepository.findByCollectorId(collectorId);

		//clean up needed for < > characters
		scopes.forEach(scope -> {
			scope.setName(COMPILE_PATTERN.matcher(scope.getName()).replaceAll(""));
			scope.setProjectPath(COMPILE_PATTERN.matcher(scope.getProjectPath()).replaceAll(""));
		});

		return scopes;
	}

	/**
	 * Finds paged results of scope items of a given collectorId, projectName, pageable
	 *
	 * @param  collectorId
	 * @param {@link org.springframework.data.domain.Pageable} object to determine which page to return
	 * @return scope items matching the specified type
	 */
	@Override
	public Page<Scope> getScopeByCollectorWithFilter(ObjectId collectorId, String projectName, Pageable pageable){

		return scopeRepository.findAllByCollectorIdAndNameContainingIgnoreCase(collectorId,projectName,pageable);
	}

}
