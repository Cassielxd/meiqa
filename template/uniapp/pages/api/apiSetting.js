
import { cache } from 'pages/utils/uniApi.js';
const setting = {
	// developeApiUrl: 'http://chat.lfmn.fun',
	developeApiUrl: `${cache.get('requestFile')}://${cache.get('dominName')}`,
	productionApiUrl: `${cache.get('requestFile')}://${cache.get('dominName')}`,
	// productionApiUrl: location.origin + '/api' : '',
	wsUrlDevelop: `${cache.get('wsFile')}://${cache.get('dominName')}/`,
	wsUrlProduction: `${cache.get('wsFile')}://${cache.get('dominName')}/`,
	// wsUrlProduction: `ws:${location.hostname}`
}

export default setting;